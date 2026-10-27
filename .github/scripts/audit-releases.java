//JAVA 25+

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

class AuditReleases {
  private static final Pattern FRONT_MATTER =
    Pattern.compile("\\A---\\R(.*?)\\R---(?:\\R|$)", Pattern.DOTALL);
  private static final Pattern RELEASE =
    Pattern.compile("(?m)^lastRelease:[ \\t]*(.*)$");
  private static final Pattern RELEASE_HREF =
    Pattern.compile("(?m)^lastReleaseHref:[ \\t]*(.*)$");
  private static final Pattern GITHUB_SOURCE =
    Pattern.compile("^https://github\\.com/([^/]+)/([^/]+)/(releases|tags)(?:/.*)?/?$");
  private static final Pattern RELEASE_TAG =
    Pattern.compile("\"tag_name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern TAG_NAME =
    Pattern.compile("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern MAVEN_RELEASE =
    Pattern.compile("<release>\\s*([^<\\s]+)\\s*</release>");
  private static final Pattern MAVEN_VERSION =
    Pattern.compile("<version>\\s*([^<\\s]+)\\s*</version>");
  private static final Pattern VERSION =
    Pattern.compile("(?i)(?:^|[^a-z0-9])v?(\\d+(?:\\.\\d+){0,3})(?![a-z0-9])");
  private static final Pattern PRERELEASE =
    Pattern.compile("(?i)(?:^|[._-])(alpha|beta|rc|snapshot|preview|milestone|dev|nightly|ea)(?:[._-]?\\d+)?(?:$|[._-])");

  private static final HttpClient HTTP = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(20))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .sslContext(sslContext())
    .build();

  private static SSLContext sslContext() {
    try {
      String certificateFile = System.getenv("SSL_CERT_FILE");
      if (certificateFile == null || certificateFile.isBlank()) {
        return SSLContext.getDefault();
      }

      TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      factory.init((KeyStore) null);
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      int index = 0;
      for (var manager : factory.getTrustManagers()) {
        if (manager instanceof X509TrustManager x509) {
          for (var certificate : x509.getAcceptedIssuers()) {
            trustStore.setCertificateEntry("default-" + index++, certificate);
          }
        }
      }
      try (var input = Files.newInputStream(Path.of(certificateFile))) {
        var certificates = CertificateFactory.getInstance("X.509").generateCertificates(input);
        if (certificates.isEmpty()) {
          throw new IOException("SSL_CERT_FILE contains no certificates");
        }
        for (var certificate : certificates) {
          trustStore.setCertificateEntry("environment-" + index++, certificate);
        }
      }
      factory.init(trustStore);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, factory.getTrustManagers(), null);
      return context;
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Could not configure release-audit TLS trust", e);
    }
  }

  public static void main(String... args) throws Exception {
    String token = System.getenv("GITHUB_TOKEN");
    List<Path> entries;
    try (Stream<Path> files = Files.list(Path.of("."))) {
      entries = files
        .filter(path -> path.getFileName().toString().endsWith(".md"))
        .filter(path -> !path.getFileName().toString().equalsIgnoreCase("README.md"))
        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
        .toList();
    }

    int checked = 0;
    int updated = 0;
    for (Path entry : entries) {
      String markdown = Files.readString(entry, StandardCharsets.UTF_8);
      Matcher frontMatterMatcher = FRONT_MATTER.matcher(markdown);
      if (!frontMatterMatcher.find()) {
        throw new IOException("Missing YAML front matter in " + entry);
      }

      String frontMatter = frontMatterMatcher.group(1);
      String currentRelease = firstValue(RELEASE, frontMatter);
      String sourceHref = firstValue(RELEASE_HREF, frontMatter);
      if (currentRelease == null || currentRelease.isBlank()) {
        System.out.println("SKIP " + entry + ": no lastRelease value");
        continue;
      }
      if (sourceHref == null || sourceHref.isBlank()) {
        System.out.println("SKIP " + entry + ": no verified lastReleaseHref");
        continue;
      }

      Source source = parseSource(sourceHref, entry);
      String currentVersion = versionOf(currentRelease);
      if (currentVersion == null) {
        System.out.println("SKIP " + entry + ": cannot identify a version in " + currentRelease);
        continue;
      }

      checked++;
      List<String> publishedVersions = getPublishedVersions(source, token);
      String latest = publishedVersions.stream()
        .filter(version -> !PRERELEASE.matcher(version).find())
        .filter(version -> versionOf(version) != null)
        .max((left, right) -> compareVersions(versionOf(left), versionOf(right)))
        .orElseThrow(() -> new IOException("No stable version tags found at " + sourceHref));

      if (compareVersions(versionOf(latest), currentVersion) > 0) {
        String updatedMarkdown = replaceRelease(markdown, latest);
        Files.writeString(entry, updatedMarkdown, StandardCharsets.UTF_8);
        System.out.println("UPDATE " + entry + ": " + currentRelease + " -> " + latest + " (" + sourceHref + ")");
        updated++;
      } else {
        System.out.println("OK " + entry + ": " + currentRelease + " (latest verified: " + latest + ")");
      }
    }

    System.out.println("Checked " + checked + " release sources; updated " + updated + " project entries.");
  }

  private static String firstValue(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group(1).trim() : null;
  }

  private static Source parseSource(String href, Path entry) throws IOException {
    Matcher matcher = GITHUB_SOURCE.matcher(href);
    if (matcher.matches()) {
      return new Source(matcher.group(1), matcher.group(2), matcher.group(3).equals("tags"), null, null);
    }

    URI uri;
    try {
      uri = URI.create(href);
    } catch (IllegalArgumentException e) {
      throw new IOException("Malformed lastReleaseHref in " + entry + ": " + href, e);
    }
    if (!"https".equals(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1 ||
        uri.getQuery() != null || uri.getFragment() != null) {
      throw new IOException("Release source URLs must be plain HTTPS URLs in " + entry + ": " + href);
    }
    String host = uri.getHost();
    String[] segments = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8).split("/");
    if (host != null && (host.equals("mvnrepository.com") || host.equals("central.sonatype.com"))) {
      int artifactPath = -1;
      for (int i = 0; i < segments.length; i++) {
        if (segments[i].equals("artifact")) {
          artifactPath = i;
          break;
        }
      }
      if (artifactPath >= 0 && segments.length > artifactPath + 2) {
        return new Source(
          null, null, false, segments[artifactPath + 1], segments[artifactPath + 2]
        );
      }
    }
    if (host != null && (host.equals("repo.maven.apache.org") || host.equals("repo1.maven.org"))) {
      int maven2 = -1;
      for (int i = 0; i < segments.length; i++) {
        if (segments[i].equals("maven2")) {
          maven2 = i;
          break;
        }
      }
      if (maven2 >= 0 && segments.length > maven2 + 2) {
        String artifact = segments[segments.length - 1].isBlank()
          ? segments[segments.length - 2]
          : segments[segments.length - 1];
        int artifactIndex = segments.length - (segments[segments.length - 1].isBlank() ? 2 : 1);
        String group = String.join(".", java.util.Arrays.copyOfRange(segments, maven2 + 1, artifactIndex));
        return new Source(null, null, false, group, artifact);
      }
    }
    throw new IOException("Unsupported or malformed lastReleaseHref in " + entry + ": " + href);
  }

  private static List<String> getPublishedVersions(Source source, String token) throws IOException, InterruptedException {
    if (source.maven()) {
      String groupPath = source.group().replace('.', '/');
      String metadataUrl = "https://repo.maven.apache.org/maven2/" + groupPath + "/" +
        source.artifact() + "/maven-metadata.xml";
      String xml = get(metadataUrl, token);
      Matcher releaseMatcher = MAVEN_RELEASE.matcher(xml);
      if (releaseMatcher.find()) {
        return List.of(releaseMatcher.group(1));
      }
      List<String> versions = new ArrayList<>();
      Matcher versionMatcher = MAVEN_VERSION.matcher(xml);
      while (versionMatcher.find()) {
        versions.add(versionMatcher.group(1));
      }
      if (versions.isEmpty()) {
        throw new IOException("Maven metadata contained no release or version entries for " + metadataUrl);
      }
      return versions;
    }
    if (!source.tags()) {
      String json = get("https://api.github.com/repos/" + source.owner() + "/" + source.repository() + "/releases/latest", token);
      Matcher matcher = RELEASE_TAG.matcher(json);
      if (!matcher.find()) {
        throw new IOException("GitHub latest-release response did not contain tag_name for " + source);
      }
      return List.of(unescapeJson(matcher.group(1)));
    }

    List<String> tags = new ArrayList<>();
    for (int page = 1; page <= 5; page++) {
      String json = get(
        "https://api.github.com/repos/" + source.owner() + "/" + source.repository() +
          "/tags?per_page=100&page=" + page,
        token
      );
      Matcher matcher = TAG_NAME.matcher(json);
      int pageCount = 0;
      while (matcher.find()) {
        tags.add(unescapeJson(matcher.group(1)));
        pageCount++;
      }
      if (pageCount < 100) {
        break;
      }
    }
    if (tags.isEmpty()) {
      throw new IOException("GitHub tags response contained no tags for " + source);
    }
    return tags;
  }

  private static String get(String url, String token) throws IOException, InterruptedException {
    URI uri = URI.create(url);
    HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
      .timeout(Duration.ofSeconds(30))
      .header("Accept", uri.getHost().equals("api.github.com") ? "application/vnd.github+json" : "application/xml")
      .header("User-Agent", "awesome-java-ui-release-audit")
      .GET();
    if (uri.getHost().equals("api.github.com")) {
      request.header("X-GitHub-Api-Version", "2022-11-28");
    }
    if (uri.getHost().equals("api.github.com") && token != null && !token.isBlank()) {
      request.header("Authorization", "Bearer " + token);
    }

    HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() != 200) {
      throw new IOException("Upstream source returned HTTP " + response.statusCode() + " for " + url);
    }
    return response.body();
  }

  private static String unescapeJson(String value) {
    return value.replace("\\\"", "\"").replace("\\\\", "\\");
  }

  private static String versionOf(String value) {
    Matcher matcher = VERSION.matcher(value);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static int compareVersions(String left, String right) {
    String[] leftParts = left.split("\\.");
    String[] rightParts = right.split("\\.");
    int count = Math.max(leftParts.length, rightParts.length);
    for (int i = 0; i < count; i++) {
      BigInteger leftPart = i < leftParts.length ? new BigInteger(leftParts[i]) : BigInteger.ZERO;
      BigInteger rightPart = i < rightParts.length ? new BigInteger(rightParts[i]) : BigInteger.ZERO;
      int comparison = leftPart.compareTo(rightPart);
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }

  private static String replaceRelease(String markdown, String latest) throws IOException {
    Matcher matcher = Pattern.compile("(?m)^(lastRelease:[ \\t]*)[^\\r\\n]*").matcher(markdown);
    if (!matcher.find()) {
      throw new IOException("Could not locate lastRelease field while updating an entry");
    }
    return matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + latest));
  }

  private record Source(String owner, String repository, boolean tags, String group, String artifact) {
    private boolean maven() {
      return group != null && artifact != null;
    }
  }
}

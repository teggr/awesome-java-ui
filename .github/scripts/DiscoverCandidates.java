//JAVA 25+
//DEPS com.fasterxml.jackson.core:jackson-databind:2.19.0

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

void main() throws Exception {
  try {
    var generatedAt = Instant.now();
    var cutoff = generatedAt.atOffset(ZoneOffset.UTC).toLocalDate().minusDays(365L * 3);
    var catalog = Discovery.readCatalog();
    var github = Discovery.githubCandidates(catalog, cutoff);
    var maven = Discovery.mavenCandidates(catalog, cutoff);
    System.out.print(Discovery.renderDigest(github, maven, generatedAt, cutoff));
  } catch (IOException | InterruptedException | RuntimeException e) {
    System.err.println("Candidate discovery failed: " + e.getMessage());
    System.exit(1);
  }
}

record Catalog(Set<String> names, Set<String> repositoryUrls) {}
record Candidate(String name, String url, String description, int stars, String updated) {}
record Artifact(String group, String name, String version, String released, String url) {}

final class Discovery {
  static final String GITHUB_API = "https://api.github.com/search/repositories";
  static final String MAVEN_API = "https://search.maven.org/solrsearch/select";
  static final int MAX_CANDIDATES_PER_SOURCE = 10;
  static final ObjectMapper JSON = new ObjectMapper();
  static final HttpClient HTTP = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(15))
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();
  static final Pattern FRONT_MATTER = Pattern.compile("^(name|learnMoreHref):\\s*(.*?)\\s*$", Pattern.MULTILINE);
  static final Pattern GITHUB_REPO = Pattern.compile(
      "https?://(?:www\\.)?github\\.com/([^/\\s]+)/([^/#?\\s]+)", Pattern.CASE_INSENSITIVE);
  static final Pattern REPO_NAME = Pattern.compile("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+");
  static final Pattern UI_CONTEXT = Pattern.compile("\\b(?:ui|gui|desktop|terminal)\\b|user interface", Pattern.CASE_INSENSITIVE);
  static final Pattern LIBRARY_TYPE = Pattern.compile(
      "\\b(?:libraries|library|frameworks?|toolkits?|components?|widgets?|controls?|engines?)\\b",
      Pattern.CASE_INSENSITIVE);
  static final List<String> RELEVANT_TERMS = List.of(
      "javafx", "swing", "swt", "lanterna", "jline", "vaadin", "webfx",
      "libgdx", "jmonkeyengine", "gluon", "teavm", "gwt");
  static final List<String> MAVEN_TERMS = List.of("javafx", "swing", "terminal");
  static final List<String> GITHUB_QUERIES = List.of(
      "java ui", "java desktop ui", "javafx component", "java terminal ui");

  private Discovery() {}

  static Catalog readCatalog() throws IOException {
    var names = new HashSet<String>();
    var repositoryUrls = new HashSet<String>();
    try (var files = Files.list(Path.of("."))) {
      for (var path : files.filter(p -> p.toString().endsWith(".md")
          && !p.getFileName().toString().equalsIgnoreCase("README.md")).toList()) {
        var text = Files.readString(path, StandardCharsets.UTF_8);
        if (!text.startsWith("---")) continue;
        var end = text.indexOf("---", 3);
        if (end < 0) continue;
        var matcher = FRONT_MATTER.matcher(text.substring(3, end));
        var fields = new HashMap<String, String>();
        while (matcher.find()) fields.put(matcher.group(1), unquote(matcher.group(2)));

        var name = fields.getOrDefault("name", "");
        if (!name.isBlank()) names.add(normalize(name));
        names.add(normalize(removeExtension(path.getFileName().toString())));

        var match = GITHUB_REPO.matcher(fields.getOrDefault("learnMoreHref", ""));
        if (match.find()) {
          var repo = (match.group(1) + "/" + match.group(2)).replaceFirst("(?i)\\.git$", "");
          repositoryUrls.add(repo.toLowerCase(Locale.ROOT));
        }
      }
    }
    return new Catalog(names, repositoryUrls);
  }

  static List<Candidate> githubCandidates(Catalog catalog, LocalDate cutoff)
      throws IOException, InterruptedException {
    var candidates = new HashMap<String, Candidate>();
    var token = System.getenv("GITHUB_TOKEN");
    for (var term : GITHUB_QUERIES) {
      var query = term + " language:Java stars:>=3 pushed:>=" + cutoff;
      var url = GITHUB_API + "?q=" + encode(query) + "&sort=updated&order=desc&per_page=10";
      var headers = token == null || token.isBlank()
          ? Map.<String, String>of()
          : Map.of("Authorization", "Bearer " + token);
      var response = requestJson(url, headers);
      for (var repo : response.path("items")) {
        var fullName = repo.path("full_name").asText("");
        var name = repo.path("name").asText(fullName);
        var description = repo.path("description").asText("");
        var updated = repo.path("pushed_at").asText("");
        if (!REPO_NAME.matcher(fullName).matches() || updated.length() < 10
            || updated.substring(0, 10).compareTo(cutoff.toString()) < 0) continue;

        var searchText = name + " " + description + " " + repo.path("topics").toString();
        if (!isRelevant(searchText)
            || catalog.repositoryUrls().contains(fullName.toLowerCase(Locale.ROOT))
            || isCatalogued(name, catalog.names())) continue;

        var safeUrl = "https://github.com/" + fullName;
        candidates.put(fullName.toLowerCase(Locale.ROOT), new Candidate(
            name, safeUrl, description, repo.path("stargazers_count").asInt(0), updated.substring(0, 10)));
      }
    }
    return candidates.values().stream()
        .sorted(Comparator.comparingInt(Candidate::stars).reversed()
            .thenComparing(Candidate::updated, Comparator.reverseOrder()))
        .limit(MAX_CANDIDATES_PER_SOURCE).toList();
  }

  static List<Artifact> mavenCandidates(Catalog catalog, LocalDate cutoff)
      throws IOException, InterruptedException {
    var candidates = new HashMap<String, Artifact>();
    for (var term : MAVEN_TERMS) {
      var url = MAVEN_API + "?q=" + encode(term)
          + "&rows=20&wt=json&sort=" + encode("timestamp desc");
      var response = requestJson(url, Map.of());
      for (var artifact : response.path("response").path("docs")) {
        var group = artifact.path("g").asText("");
        var name = artifact.path("a").asText("");
        var version = artifact.path("latestVersion").asText("");
        if (group.isBlank() || name.isBlank() || version.isBlank()
            || !artifact.hasNonNull("timestamp")) continue;
        var released = Instant.ofEpochMilli(artifact.path("timestamp").asLong())
            .atOffset(ZoneOffset.UTC).toLocalDate();
        if (released.isBefore(cutoff) || !isRelevant(group + " " + name)) continue;
        var normalizedName = normalize(name);
        if (isCatalogued(name, catalog.names())
            || catalog.names().stream().anyMatch(existing -> existing.length() >= 4
                && normalizedName.startsWith(existing))) continue;

        var key = (group + ":" + name).toLowerCase(Locale.ROOT);
        var safeUrl = "https://central.sonatype.com/artifact/" + encode(group) + "/" + encode(name);
        candidates.put(key, new Artifact(group, name, version, released.toString(), safeUrl));
      }
    }
    return candidates.values().stream()
        .sorted(Comparator.comparing(Artifact::released).reversed())
        .limit(MAX_CANDIDATES_PER_SOURCE).toList();
  }

  static JsonNode requestJson(String url, Map<String, String> headers)
      throws IOException, InterruptedException {
    IOException lastFailure = null;
    for (int attempt = 0; attempt < 3; attempt++) {
      try {
        var requestBuilder = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "awesome-java-ui-candidate-discovery")
            .GET();
        headers.forEach(requestBuilder::header);
        var response = HTTP.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
          return JSON.readTree(response.body());
        }
        var error = new IOException("HTTP " + response.statusCode() + " from " + url);
        if (response.statusCode() < 500 && response.statusCode() != 429) throw error;
        lastFailure = error;
      } catch (java.net.http.HttpTimeoutException e) {
        lastFailure = new IOException("request timed out: " + url, e);
      } catch (java.net.ConnectException e) {
        lastFailure = new IOException("connection failed: " + url, e);
      }
      if (attempt < 2) Thread.sleep(1000L << attempt);
    }
    throw new IOException("Request to " + url + " failed after 3 attempts", lastFailure);
  }

  static String renderDigest(List<Candidate> github, List<Artifact> maven, Instant generatedAt, LocalDate cutoff) {
    var lines = new ArrayList<String>();
    lines.add("Automated discovery run on " + generatedAt.atOffset(ZoneOffset.UTC).toLocalDate()
        + " (UTC). These are unverified leads, not recommendations or accepted catalogue entries.");
    lines.add("");
    lines.add("**Sources:** GitHub repository search and Maven Central. The collector considers projects updated or artifacts released since "
        + cutoff + ", with GitHub repositories requiring at least 3 stars.");
    lines.add("");
    lines.add("Please verify that each project is a Java UI library/framework and is not already represented before adding it.");
    lines.add("");
    lines.add("## GitHub repositories");
    lines.add("");
    if (github.isEmpty()) {
      lines.add("_No new candidates found._");
    } else {
      for (var candidate : github) {
        lines.add("- [" + markdown(candidate.name()) + "](" + candidate.url() + ") ("
            + candidate.stars() + " stars; pushed " + candidate.updated() + ")");
        if (!candidate.description().isBlank()) {
          lines.add("  - " + markdown(candidate.description()));
        }
      }
    }
    lines.add("");
    lines.add("## Maven Central artifacts");
    lines.add("");
    if (maven.isEmpty()) {
      lines.add("_No new candidates found._");
    } else {
      for (var artifact : maven) {
        lines.add("- [" + markdown(artifact.group() + ":" + artifact.name()) + "]("
            + artifact.url() + ") (latest version " + markdown(artifact.version())
            + "; released " + artifact.released() + ")");
      }
    }
    lines.add("");
    lines.add("---");
    lines.add("To suggest an entry, verify the official project/documentation URL and use the "
        + "[new-project issue template](https://github.com/teggr/awesome-java-ui/issues/new?template=onboard-new-project.md).");
    lines.add("");
    return String.join("\n", lines);
  }

  static boolean isRelevant(String value) {
    var hasUiContext = UI_CONTEXT.matcher(value).find();
    var hasLibraryType = LIBRARY_TYPE.matcher(value).find();
    var normalized = normalize(value);
    var knownTechnology = RELEVANT_TERMS.stream().map(Discovery::normalize)
        .anyMatch(normalized::contains);
    return hasLibraryType && (hasUiContext || knownTechnology);
  }

  static boolean isCatalogued(String name, Set<String> names) {
    return names.contains(normalize(name));
  }

  static String normalize(String value) {
    return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
  }

  static String markdown(String value) {
    return value.replaceAll("\\s+", " ").strip()
        .replace("\\", "\\\\").replace("`", "\\`").replace("*", "\\*")
        .replace("_", "\\_").replace("{", "\\{").replace("}", "\\}")
        .replace("[", "\\[").replace("]", "\\]").replace("#", "\\#")
        .replace("+", "\\+").replace("-", "\\-").replace(".", "\\.")
        .replace("!", "\\!").replace("|", "\\|").replace(">", "\\>");
  }

  static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  static String unquote(String value) {
    var trimmed = value.strip();
    if (trimmed.length() >= 2 && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
        || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
      return trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }

  static String removeExtension(String filename) {
    return filename.endsWith(".md") ? filename.substring(0, filename.length() - 3) : filename;
  }
}

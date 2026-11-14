///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Weekly social publishing.
 *
 *   jbang social/SocialPost.java generate [--reshuffle]   build/refresh queue and drafted tweets
 *   jbang social/SocialPost.java post [--dry-run]         post the next tweet and advance state
 *
 * Files (reviewable and editable by hand):
 *   social/queue.txt   posting order, one project slug per line
 *   social/tweets.txt  slug TAB tweet text (newlines encoded as \n)
 *   social/state.txt   index of the next queue entry to post
 */
public class SocialPost {

  static final String SITE = "https://awesome-java-ui.com/";
  static final int MAX = 280;
  static final int URL_LENGTH = 23; // t.co wraps every link
  static final Path DIR = Path.of("social");
  static final Path QUEUE = DIR.resolve("queue.txt");
  static final Path TWEETS = DIR.resolve("tweets.txt");
  static final Path STATE = DIR.resolve("state.txt");

  public static void main(String[] args) throws Exception {
    String cmd = args.length > 0 ? args[0] : "";
    List<String> flags = List.of(args);
    switch (cmd) {
      case "generate" -> generate(flags.contains("--reshuffle"));
      case "post" -> post(flags.contains("--dry-run"));
      default -> {
        System.err.println("Usage: generate [--reshuffle] | post [--dry-run]");
        System.exit(2);
      }
    }
  }

  // ---------- generate ----------

  static void generate(boolean reshuffle) throws IOException {
    Files.createDirectories(DIR);
    Map<String, String> drafts = new LinkedHashMap<>();
    try (Stream<Path> files = Files.list(Path.of("."))) {
      for (Path p : files.sorted().toList()) {
        String file = p.getFileName().toString();
        if (!file.endsWith(".md") || file.equals("README.md")) continue;
        String slug = file.substring(0, file.length() - 3);
        drafts.put(slug, draft(slug, Files.readString(p)));
      }
    }

    boolean resume = Files.exists(QUEUE) && !reshuffle;
    List<String> queue = resume ? readLines(QUEUE) : new ArrayList<>();
    int state = resume ? readState() : 0;

    // prune removed projects, shifting the position for entries removed before it
    List<String> kept = new ArrayList<>();
    for (int i = 0; i < queue.size(); i++) {
      if (drafts.containsKey(queue.get(i))) kept.add(queue.get(i));
      else if (i < state) state--;
    }
    // append new projects in random order
    List<String> added = new ArrayList<>(drafts.keySet());
    added.removeAll(kept);
    Collections.shuffle(added);
    kept.addAll(added);
    if (state >= kept.size()) state = 0;

    // keep manual edits to existing drafts; only new slugs get fresh drafts
    Map<String, String> existing = resume && Files.exists(TWEETS) ? readTweets() : Map.of();
    StringBuilder out = new StringBuilder();
    for (String slug : kept) {
      String text = existing.getOrDefault(slug, drafts.get(slug));
      out.append(slug).append('\t').append(text.replace("\n", "\\n")).append('\n');
    }

    Files.writeString(QUEUE, String.join("\n", kept) + "\n");
    Files.writeString(TWEETS, out.toString());
    Files.writeString(STATE, state + "\n");
    System.out.println("Queue: " + kept.size() + " projects (" + added.size() + " new), next index " + state);
  }

  static String draft(String slug, String markdown) {
    String name = slug;
    String body = "";
    String[] parts = markdown.split("(?m)^---\\s*$", 3);
    if (parts.length == 3) {
      for (String line : parts[1].split("\n")) {
        if (line.startsWith("name:")) name = line.substring(5).trim();
      }
      body = parts[2];
    }
    String description = "";
    for (String para : body.split("\n\\s*\n")) {
      String t = para.strip();
      if (!t.isEmpty() && !t.startsWith("#") && !t.startsWith("```")) {
        description = t.replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "$1").replaceAll("[*_`]", "").replaceAll("\\s+", " ");
        break;
      }
    }
    String url = SITE + slug + ".html";
    String head = name + " - ";
    String tail = "\n\n" + url + "\n#Java #JavaUI";
    int budget = MAX - head.length() - (tail.length() - url.length() + URL_LENGTH);
    String text = description.length() <= budget ? description : truncate(description, budget);
    return head + text + tail;
  }

  static String truncate(String s, int budget) {
    String cut = s.substring(0, Math.max(0, budget - 1));
    int space = cut.lastIndexOf(' ');
    if (space > budget / 2) cut = cut.substring(0, space);
    return cut.replaceAll("[\\s,;:.-]+$", "") + "…";
  }

  // ---------- post ----------

  static void post(boolean dryRun) throws Exception {
    List<String> queue = readLines(QUEUE);
    Map<String, String> tweets = readTweets();
    int state = readState();
    if (queue.isEmpty()) throw new IllegalStateException("Queue is empty; run generate first");
    if (state >= queue.size()) state = 0; // cycle forever
    String slug = queue.get(state);
    String text = tweets.get(slug);
    if (text == null) throw new IllegalStateException("No tweet drafted for " + slug);
    String counted = text.replaceAll("https?://\\S+", "x".repeat(URL_LENGTH));
    int length = counted.codePointCount(0, counted.length());
    if (length > MAX) throw new IllegalStateException("Tweet for " + slug + " is " + length + " characters");

    System.out.println("Next tweet (" + slug + ", " + length + " chars):\n" + text);
    if (dryRun) return;

    System.out.println("Posted tweet " + tweet(text));
    Files.writeString(STATE, ((state + 1) % queue.size()) + "\n"); // only after success
  }

  static String tweet(String text) throws Exception {
    String url = "https://api.twitter.com/2/tweets";
    String json = "{\"text\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
        .header("Authorization", oauthHeader("POST", url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 201) {
      throw new IOException("Twitter API returned " + response.statusCode() + ": " + response.body());
    }
    var m = Pattern.compile("\"id\":\"(\\d+)\"").matcher(response.body());
    return m.find() ? m.group(1) : "?";
  }

  // OAuth 1.0a user context; JSON bodies are not part of the signature base string
  static String oauthHeader(String method, String url) throws Exception {
    String consumerSecret = env("X_API_SECRET");
    String tokenSecret = env("X_ACCESS_TOKEN_SECRET");

    byte[] nonceBytes = new byte[16];
    new SecureRandom().nextBytes(nonceBytes);
    Map<String, String> p = new TreeMap<>();
    p.put("oauth_consumer_key", env("X_API_KEY"));
    p.put("oauth_nonce", Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes));
    p.put("oauth_signature_method", "HMAC-SHA1");
    p.put("oauth_timestamp", String.valueOf(Instant.now().getEpochSecond()));
    p.put("oauth_token", env("X_ACCESS_TOKEN"));
    p.put("oauth_version", "1.0");

    String params = p.entrySet().stream()
        .map(e -> enc(e.getKey()) + "=" + enc(e.getValue())).collect(Collectors.joining("&"));
    String base = method + "&" + enc(url) + "&" + enc(params);
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(new SecretKeySpec((enc(consumerSecret) + "&" + enc(tokenSecret)).getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
    p.put("oauth_signature", Base64.getEncoder().encodeToString(mac.doFinal(base.getBytes(StandardCharsets.UTF_8))));

    return "OAuth " + p.entrySet().stream()
        .map(e -> enc(e.getKey()) + "=\"" + enc(e.getValue()) + "\"").collect(Collectors.joining(", "));
  }

  // ---------- helpers ----------

  static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
  }

  static String env(String name) {
    String v = System.getenv(name);
    if (v == null || v.isBlank()) throw new IllegalStateException("Missing environment variable " + name);
    return v;
  }

  static List<String> readLines(Path p) throws IOException {
    return Files.readAllLines(p).stream().map(String::strip).filter(s -> !s.isEmpty())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  static int readState() throws IOException {
    return Files.exists(STATE) ? Integer.parseInt(Files.readString(STATE).strip()) : 0;
  }

  static Map<String, String> readTweets() throws IOException {
    Map<String, String> m = new HashMap<>();
    for (String line : Files.readAllLines(TWEETS)) {
      int tab = line.indexOf('\t');
      if (tab > 0) m.put(line.substring(0, tab), line.substring(tab + 1).replace("\\n", "\n"));
    }
    return m;
  }
}

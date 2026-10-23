/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS com.j2html:j2html:1.6.0
//DEPS org.commonmark:commonmark:0.21.0
//DEPS org.commonmark:commonmark-ext-yaml-front-matter:0.21.0
//DEPS org.commonmark:commonmark-ext-heading-anchor:0.21.0

import j2html.rendering.IndentedHtml;
import j2html.tags.DomContent;
import j2html.tags.specialized.HtmlTag;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static j2html.TagCreator.*;

void main(String... args) throws IOException {

  System.out.println("Building Awesome Java UI static site...");

  // Create docs directory if it doesn't exist
  Path outputDirectory = Paths.get("_site");
  if (!Files.exists(outputDirectory)) {
    Files.createDirectory(outputDirectory);
    System.out.println("Created output directory");
  }

  // collect all markdown data in a map, key is html file name, value is the data from the markdown file
  Map<String, Map<String, List<String>>> markdownData = new TreeMap<>();

  // start iterating over the markdown files in the root folder, excluding README.md
  Files.list(Paths.get("."))
    .filter(path -> path.toString().endsWith(".md") && !path.getFileName().toString().equals("README.md"))
    .forEach(path -> {
      try {
        System.out.println("Processing " + path);
        String markdown = Files.readString(path);

        // Parse markdown with CommonMark
        Parser parser = Parser.builder()
          .extensions(List.of(YamlFrontMatterExtension.create(), HeadingAnchorExtension.create()))
          .build();
        HtmlRenderer renderer = HtmlRenderer.builder()
          .extensions(List.of(YamlFrontMatterExtension.create(), HeadingAnchorExtension.create()))
          .build();
        org.commonmark.node.Node document = parser.parse(markdown);
        // Extract YAML front matter
        YamlFrontMatterVisitor frontMatterVisitor = new YamlFrontMatterVisitor();
        document.accept(frontMatterVisitor);

        String html = renderer.render(document);
        Map<String, List<String>> projectData = new HashMap<>(frontMatterVisitor.getData());
        projectData.put("_description", List.of(extractDescription(html)));
        // Fix formatting: add newline after opening code tag for proper indentation
        html = html.replaceAll("(<code[^>]*>)([^ \\n])", "$1\n$2");

        // Write HTML to docs directory
        HtmlTag htmlTag = output(project(projectData, rawHtml(html)),
          SeoMetadata.forProject(projectData, path.getFileName().toString().replace(".md", ".html")));

        StringWriter writer = new StringWriter();
        writer.append(htmlTag.render(IndentedHtml.inMemory()));

        String finalHtml = writer.toString();

        String htmlFileName = path.getFileName().toString().replace(".md", ".html");

        markdownData.put(htmlFileName, projectData);

        Path htmlPath = outputDirectory.resolve(htmlFileName);
        Files.writeString(htmlPath, finalHtml);
        System.out.println("Generated " + htmlFileName);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

  // Generate index.html
  StringWriter writer = new StringWriter();
  writer.append(output(indexPage(markdownData), SeoMetadata.index()).render(IndentedHtml.inMemory()));
  String html = writer.toString();
  Path indexPath = outputDirectory.resolve("index.html");
  Files.writeString(indexPath, html);
  System.out.println("Generated index.html");

  // Generate grid partials for htmx sorting
  StringWriter gridAlphaWriter = new StringWriter();
  gridAlphaWriter.append(gridAlphabetical(markdownData).render(IndentedHtml.inMemory()));
  Files.writeString(outputDirectory.resolve("grid-alphabetical.html"), gridAlphaWriter.toString());
  System.out.println("Generated grid-alphabetical.html");

  StringWriter gridTagWriter = new StringWriter();
  gridTagWriter.append(gridByTag(markdownData).render(IndentedHtml.inMemory()));
  Files.writeString(outputDirectory.resolve("grid-by-tag.html"), gridTagWriter.toString());
  System.out.println("Generated grid-by-tag.html");

  StringWriter gridDateWriter = new StringWriter();
  gridDateWriter.append(gridByDate(markdownData).render(IndentedHtml.inMemory()));
  Files.writeString(outputDirectory.resolve("grid-by-date.html"), gridDateWriter.toString());
  System.out.println("Generated grid-by-date.html");

  // Collect unique tags from all markdown data
  Set<String> uniqueTags = collectUniqueTags(markdownData);

  // Generate tag pages
  for (String tag : uniqueTags) {
    String tagSlug = tagToSlug(tag);
    String tagFileName = "tag-" + tagSlug + ".html";
    StringWriter tagWriter = new StringWriter();
    tagWriter.append(output(tagPage(tag, markdownData), SeoMetadata.forTag(tag)).render(IndentedHtml.inMemory()));
    String tagHtml = tagWriter.toString();
    Path tagPath = outputDirectory.resolve(tagFileName);
    Files.writeString(tagPath, tagHtml);
    System.out.println("Generated " + tagFileName + " for tag: " + tag);
  }

  // copy the css directory and all the files inside to the output directory
  Path cssSource = Paths.get("css");
  Path cssTarget = outputDirectory.resolve("css");
  if (Files.exists(cssSource) && Files.isDirectory(cssSource)) {
    Files.walk(cssSource).forEach(source -> {
      try {
        Path target = cssTarget.resolve(cssSource.relativize(source));
        if (Files.isDirectory(source)) {
          if (!Files.exists(target)) {
            Files.createDirectory(target);
          }
        } else {
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    System.out.println("Copied css directory");
  } else {
    System.out.println("No css directory found, skipping copy");
  }

  // copy the images directory and all the files inside to the output directory
  Path imagesSource = Paths.get("images");
  Path imagesTarget = outputDirectory.resolve("images");
  if (Files.exists(imagesSource) && Files.isDirectory(imagesSource)) {
    Files.walk(imagesSource).forEach(source -> {
      try {
        Path target = imagesTarget.resolve(imagesSource.relativize(source));
        if (Files.isDirectory(source)) {
          if (!Files.exists(target)) {
            Files.createDirectory(target);
          }
        } else {
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    System.out.println("Copied images directory");
  } else {
    System.out.println("No images directory found, skipping copy");
  }

  System.out.println("Site build completed successfully!");

}

private DomContent project(Map<String, List<String>> data, DomContent content) {
  String name = data.getOrDefault("name", List.of("Unknown Project")).get(0);
  String status = data.getOrDefault("status", List.of()).stream().findFirst().orElse("");
  String javaVersion = data.getOrDefault("javaVersion", List.of()).stream().findFirst().orElse("");
  String learningCurve = data.getOrDefault("learningCurve", List.of()).stream().findFirst().orElse("");
  String lastRelease = data.getOrDefault("lastRelease", List.of()).stream().findFirst().orElse("");
  String lastReleaseHref = data.getOrDefault("lastReleaseHref", List.of()).stream().findFirst().orElse("");
  String learnMoreText = data.getOrDefault("learnMoreText", List.of("Official project")).get(0);
  String learnMoreHref = data.getOrDefault("learnMoreHref", List.of()).stream().findFirst().orElse("");
  String image = data.getOrDefault("image", List.of()).stream().findFirst().orElse("");
  String description = data.getOrDefault("_description", List.of()).stream().findFirst().orElse("");
  List<String> tags = data.getOrDefault("tags", List.of());

  List<DomContent> facts = new ArrayList<>();
  if (!status.isBlank()) facts.add(div(span("Status"), strong(status)).withClass("project-fact"));
  if (!javaVersion.isBlank()) facts.add(div(span("Java"), strong(javaVersion)).withClass("project-fact"));
  if (!learningCurve.isBlank()) facts.add(div(span("Learning curve"), strong(learningCurve)).withClass("project-fact"));
  if (!lastRelease.isBlank()) {
    facts.add(div(
      span("Latest release"),
      lastReleaseHref.isBlank()
        ? strong(lastRelease)
        : a(lastRelease).withHref(lastReleaseHref).withTarget("_blank")
            .attr("rel", "noopener noreferrer")
    ).withClass("project-fact"));
  }

  return j2html.TagCreator.main(
    div(
      a("← All projects").withHref("index.html#browse-section").withClass("back-link")
    ).withClass("project-back").attr("data-pagefind-ignore", ""),
    section(
      div(
        span(tags.isEmpty() ? "Java UI project" : tags.get(0)).withClass("eyebrow"),
        h1(name).attr("data-pagefind-meta", "title"),
        p(description).withClass("project-lede"),
        div(
          learnMoreHref.isBlank()
            ? span("Official link not provided").withClass("project-official-link")
            : a(text(learnMoreText), i().withClass("bi bi-arrow-up-right"))
                .withHref(learnMoreHref).withTarget("_blank")
                .attr("rel", "noopener noreferrer")
                .withClass("project-official-link"),
          a("Browse all projects →").withHref("index.html#browse-section").withClass("project-directory-link")
        ).withClass("project-actions"),
        div(each(facts.stream())).withClass("project-facts"),
        div(
          each(tags, tag -> {
            String tagFileName = "tag-" + tagToSlug(tag) + ".html";
            return a(tag)
              .withHref(tagFileName)
              .attr("hx-get", tagFileName)
              .attr("hx-target", "body")
              .attr("hx-swap", "innerHTML transition:true show:window:top")
              .attr("hx-push-url", "true")
              .withClass("tag");
          })
        ).withClass("tags project-tags")
      ).withClass("project-heading"),
      image.isBlank()
        ? span().withClass("project-hero-image project-image-missing")
        : img().withSrc(image).withAlt(name + " interface")
            .attr("data-pagefind-meta", "thumbnail:" + getThumbnailUrl(image))
            .attr("fetchpriority", "high")
            .withClass("project-hero-image")
    ).withClass("project-overview"),
    article(content).withClass("project-copy")
  ).withId("main-content").attr("data-pagefind-body", "");
}

private static String extractDescription(String html) {
  int paragraphStart = html.indexOf("<p>");
  if (paragraphStart < 0) return "";
  int textStart = paragraphStart + 3;
  int paragraphEnd = html.indexOf("</p>", textStart);
  if (paragraphEnd < 0) return "";
  String description = html.substring(textStart, paragraphEnd)
    .replaceAll("<[^>]*>", " ")
    .replaceAll("&[a-zA-Z#0-9]+;", " ")
    .replaceAll("\\s+", " ")
    .trim();
  if (description.length() <= 190) return description;
  int cutoff = description.lastIndexOf(' ', 190);
  return description.substring(0, cutoff > 0 ? cutoff : 190).trim() + "...";
}

private static Set<String> collectUniqueTags(Map<String, Map<String, List<String>>> markdownData) {
  Set<String> uniqueTags = new HashSet<>();
  for (Map<String, List<String>> data : markdownData.values()) {
    List<String> tags = data.getOrDefault("tags", List.of());
    uniqueTags.addAll(tags);
  }
  return uniqueTags;
}

private static String tagToSlug(String tag) {
  return tag.toLowerCase()
    .replaceAll("\\s+", "-")
    .replaceAll("[^a-z0-9-]", "");
}

private static DomContent projectCard(String htmlFileName, Map<String, List<String>> data) {
  String name = data.getOrDefault("name", List.of("Unknown Project")).get(0);
  String image = data.getOrDefault("image", List.of()).stream().findFirst().orElse("");
  String imageUrl = getThumbnailUrl(image);
  String description = data.getOrDefault("_description", List.of()).stream().findFirst().orElse("");
  String learnMoreText = data.getOrDefault("learnMoreText", List.of("Official project")).get(0);
  String learnMoreHref = data.getOrDefault("learnMoreHref", List.of()).stream().findFirst().orElse("");
  List<String> tags = data.getOrDefault("tags", List.of());

  return article(
    a(
      img().withSrc(imageUrl).withAlt(name + " interface")
        .attr("loading", "lazy")
        .attr("decoding", "async")
        .withClass("project-thumbnail")
    )
      .withHref(htmlFileName)
      .attr("hx-get", htmlFileName)
      .attr("hx-target", "body")
      .attr("hx-swap", "innerHTML transition:true show:window:top")
      .attr("hx-push-url", "true")
      .withClass("project-card-media"),
    div(
      tags.isEmpty() ? span("Java UI").withClass("project-card-tag") : span(tags.get(0)).withClass("project-card-tag"),
      h3(a(name).withHref(htmlFileName)
        .attr("hx-get", htmlFileName)
        .attr("hx-target", "body")
        .attr("hx-swap", "innerHTML transition:true show:window:top")
        .attr("hx-push-url", "true")),
      p(description),
      div(
        learnMoreHref.isBlank()
          ? span("Official link not provided").withClass("project-card-official")
          : a(text(learnMoreText), i().withClass("bi bi-arrow-up-right"))
              .withHref(learnMoreHref).withTarget("_blank")
              .attr("rel", "noopener noreferrer")
              .withClass("project-card-official"),
        a("Project details").withHref(htmlFileName).withClass("project-card-details")
      ).withClass("project-card-actions")
    ).withClass("project-card-copy")
  ).withClass("project-card");
}

private static long countProjectsWithTag(Map<String, Map<String, List<String>>> markdownData, String tag) {
  return markdownData.values().stream()
    .filter(data -> data.getOrDefault("tags", List.of()).contains(tag))
    .count();
}

private static DomContent categoryPanel(Map<String, Map<String, List<String>>> markdownData, String tag, String icon, String style) {
  String tagFileName = "tag-" + tagToSlug(tag) + ".html";
  String examples = markdownData.entrySet().stream()
    .filter(entry -> entry.getValue().getOrDefault("tags", List.of()).contains(tag))
    .map(entry -> entry.getValue().getOrDefault("name", List.of()).get(0))
    .sorted()
    .limit(3)
    .collect(java.util.stream.Collectors.joining(" / "));
  return a(
    i().withClass("bi " + icon).attr("aria-hidden", "true"),
    div(
      span(tag).withClass("category-title"),
      span(examples).withClass("category-examples"),
      span(countProjectsWithTag(markdownData, tag) + " projects").withClass("category-count")
    ),
    span("Explore →").withClass("category-arrow")
  )
    .withHref(tagFileName)
    .attr("hx-get", tagFileName)
    .attr("hx-target", "body")
    .attr("hx-swap", "innerHTML transition:true show:window:top")
    .attr("hx-push-url", "true")
    .withClass("category-panel " + style);
}

private static DomContent heroPreview(String fileName, Map<String, Map<String, List<String>>> markdownData, String style) {
  Map<String, List<String>> data = markdownData.get(fileName);
  String name = data.getOrDefault("name", List.of("Java UI project")).get(0);
  String image = data.getOrDefault("image", List.of()).stream().findFirst().orElse("");
  return figure(
    a(
      img().withSrc(getThumbnailUrl(image)).withAlt(name + " interface preview")
        .attr("loading", "eager")
        .attr("fetchpriority", "high")
    ).withHref(fileName),
    figcaption(
      span(name),
      i().withClass("bi bi-arrow-up-right").attr("aria-hidden", "true")
    )
  ).withClass("hero-preview " + style);
}

/**
 * Get the thumbnail URL for an image, falling back to the original if thumbnail doesn't exist
 */
private static String getThumbnailUrl(String originalImageUrl) {
  // Skip if it's a placeholder or external URL
  if (!originalImageUrl.startsWith("images/") || originalImageUrl.contains("placeholder")) {
    return originalImageUrl;
  }
  
  // Extract the filename after the last slash (e.g., "ui-casciian.png" -> "ui-casciian.png")
  String filename = originalImageUrl.substring(originalImageUrl.lastIndexOf('/') + 1);
  if (filename.startsWith("ui-")) {
    String thumbnailName = "thumbnail-" + filename.substring("ui-".length());
    String thumbnailUrl = "images/" + thumbnailName;
    
    // Check if thumbnail exists
    Path thumbnailPath = Paths.get(thumbnailUrl);
    if (Files.exists(thumbnailPath)) {
      return thumbnailUrl;
    }
  }
  
  // Fallback to original
  return originalImageUrl;
}

static DomContent tagPage(String tag, Map<String, Map<String, List<String>>> markdownData) {
  // Filter projects by tag
  Map<String, Map<String, List<String>>> filteredProjects = new TreeMap<>();
  for (Map.Entry<String, Map<String, List<String>>> entry : markdownData.entrySet()) {
    List<String> projectTags = entry.getValue().getOrDefault("tags", List.of());
    if (projectTags.contains(tag)) {
      filteredProjects.put(entry.getKey(), entry.getValue());
    }
  }
  
  return j2html.TagCreator.main(
    div(
      a("← All projects").withHref("index.html#browse-section").withClass("back-link")
    ).withClass("tag-page-back"),
    section(
      span("Browse by category").withClass("section-kicker"),
      h1(tag).withClass("tag-page-title"),
      p("Explore " + filteredProjects.size() + " projects tagged " + tag + ".")
        .withClass("tag-page-description")
    ).withClass("tag-page-heading"),
    div(
      each(filteredProjects.entrySet(), entry -> projectCard(entry.getKey(), entry.getValue()))
    ).withClass("project-list").withId("tag-projects")
  ).withId("main-content").withClass("tag-page");
}

// Grid content for initial page render (no OOB button)
static DomContent gridAlphabeticalContent(Map<String, Map<String, List<String>>> markdownData) {
  return div(
    each(markdownData.entrySet(), entry -> projectCard(entry.getKey(), entry.getValue()))
  ).withClass("project-list").withId("browse-section");
}

// Partial response for htmx: alphabetical grid + OOB button swap
static DomContent gridAlphabetical(Map<String, Map<String, List<String>>> markdownData) {
  return each(
    gridAlphabeticalContent(markdownData),
    sortButtons("alphabetical")
  );
}

// Grid content for tag-grouped initial page render (no OOB button)
static DomContent gridByTagContent(Map<String, Map<String, List<String>>> markdownData) {
  Set<String> uniqueTags = new TreeSet<>(collectUniqueTags(markdownData));

  return div(
    each(uniqueTags, tag -> {
      // Filter projects by tag
      Map<String, Map<String, List<String>>> filteredProjects = new TreeMap<>();
      for (Map.Entry<String, Map<String, List<String>>> entry : markdownData.entrySet()) {
        List<String> projectTags = entry.getValue().getOrDefault("tags", List.of());
        if (projectTags.contains(tag)) {
          filteredProjects.put(entry.getKey(), entry.getValue());
        }
      }
      return div(
        h2(tag).withClass("tag-group-title"),
        div(
          each(filteredProjects.entrySet(), entry -> projectCard(entry.getKey(), entry.getValue()))
        ).withClass("project-list")
      ).withClass("tag-group");
    })
  ).withId("browse-section");
}

// Partial response for htmx: tag-grouped grid + OOB button swap
static DomContent gridByTag(Map<String, Map<String, List<String>>> markdownData) {
  return each(
    gridByTagContent(markdownData),
    sortButtons("platform")
  );
}

// Grid content for date-sorted initial page render
static DomContent gridByDateContent(Map<String, Map<String, List<String>>> markdownData) {
  // Sort by dateAdded (most recent first)
  Map<String, Map<String, List<String>>> sortedData = new LinkedHashMap<>();
  markdownData.entrySet().stream()
    .sorted((e1, e2) -> {
      String date1 = e1.getValue().getOrDefault("dateAdded", List.of("1900-01-01")).get(0);
      String date2 = e2.getValue().getOrDefault("dateAdded", List.of("1900-01-01")).get(0);
      return date2.compareTo(date1); // Reverse order for most recent first
    })
    .forEach(entry -> sortedData.put(entry.getKey(), entry.getValue()));
  
  return div(
    each(sortedData.entrySet(), entry -> projectCard(entry.getKey(), entry.getValue()))
  ).withClass("project-list").withId("browse-section");
}

// Partial response for htmx: date-sorted grid + OOB button swap
static DomContent gridByDate(Map<String, Map<String, List<String>>> markdownData) {
  return each(
    gridByDateContent(markdownData),
    sortButtons("date")
  );
}

private static List<String> rotatingProjectFiles(
  Map<String, Map<String, List<String>>> markdownData,
  List<String> categories,
  LocalDate rotationDate
) {
  Set<String> selected = new LinkedHashSet<>();
  List<Map.Entry<String, Map<String, List<String>>>> eligibleProjects = markdownData.entrySet().stream()
    .filter(entry -> {
      String status = String.join(" ", entry.getValue().getOrDefault("status", List.of()))
        .toLowerCase(Locale.ROOT);
      return !status.contains("archived")
        && !status.contains("legacy")
        && !status.contains("no longer maintained");
    })
    .sorted(Map.Entry.comparingByKey())
    .toList();

  for (int categoryIndex = 0; categoryIndex < categories.size(); categoryIndex++) {
    String category = categories.get(categoryIndex);
    List<String> candidates = eligibleProjects.stream()
      .filter(entry -> entry.getValue().getOrDefault("tags", List.of()).contains(category))
      .map(Map.Entry::getKey)
      .toList();

    if (candidates.isEmpty()) {
      continue;
    }

    int startIndex = (int) Math.floorMod(
      rotationDate.toEpochDay() + categoryIndex,
      (long) candidates.size()
    );
    for (int offset = 0; offset < candidates.size(); offset++) {
      String candidate = candidates.get((startIndex + offset) % candidates.size());
      if (selected.add(candidate)) {
        break;
      }
    }
  }

  if (selected.size() < categories.size()) {
    List<String> remainingCandidates = eligibleProjects.stream()
      .map(Map.Entry::getKey)
      .toList();
    int startIndex = remainingCandidates.isEmpty()
      ? 0
      : (int) Math.floorMod(rotationDate.toEpochDay(), (long) remainingCandidates.size());
    for (int offset = 0; offset < remainingCandidates.size() && selected.size() < categories.size(); offset++) {
      selected.add(remainingCandidates.get((startIndex + offset) % remainingCandidates.size()));
    }
  }

  if (selected.size() < categories.size()) {
    throw new IllegalStateException("Not enough eligible projects to fill the homepage rotation");
  }
  return List.copyOf(selected);
}

// Generate the segmented control buttons for sorting with proper active state
static DomContent sortButtons(String activeSort) {
  return div(
    button(
      i().withClass("bi bi-sort-alpha-down"),
      text(" Alphabetical")
    )
      .withClass("sort-btn" + ("alphabetical".equals(activeSort) ? " active" : ""))
      .attr("hx-get", "grid-alphabetical.html")
      .attr("hx-target", "#browse-section")
      .attr("hx-swap", "outerHTML transition:true"),
    button(
      i().withClass("bi bi-tags"),
      text(" Platform")
    )
      .withClass("sort-btn" + ("platform".equals(activeSort) ? " active" : ""))
      .attr("hx-get", "grid-by-tag.html")
      .attr("hx-target", "#browse-section")
      .attr("hx-swap", "outerHTML transition:true"),
    button(
      i().withClass("bi bi-clock-history"),
      text(" Recently Added")
    )
      .withClass("sort-btn" + ("date".equals(activeSort) ? " active" : ""))
      .attr("hx-get", "grid-by-date.html")
      .attr("hx-target", "#browse-section")
      .attr("hx-swap", "outerHTML transition:true")
  )
    .withClass("sort-button-group")
    .withId("sort-buttons")
    .attr("hx-swap-oob", "true");
}

static DomContent indexPage(Map<String, Map<String, List<String>>> markdownData) {
  Set<String> uniqueTags = new TreeSet<>(collectUniqueTags(markdownData));
  LocalDate rotationDate = LocalDate.now(ZoneOffset.UTC);
  List<String> heroFiles = rotatingProjectFiles(
    markdownData,
    List.of("Desktop UI", "Web UI", "Mobile UI"),
    rotationDate
  );
  List<String> featuredFiles = rotatingProjectFiles(
    markdownData,
    List.of("Desktop UI", "Web UI", "Mobile UI", "Terminal UI", "Game Development"),
    rotationDate
  );
  String[][] categories = {
    {"Desktop UI", "bi-display", "category-orange"},
    {"Web UI", "bi-globe2", "category-blue"},
    {"Mobile UI", "bi-phone", "category-pink"},
    {"Terminal UI", "bi-terminal", "category-yellow"}
  };

  return j2html.TagCreator.main(
    section(
      div(
        span("Explore · Discover · Build").withClass("hero-eyebrow"),
        h1(
          span("JAVA UI").withClass("hero-title-line"),
          span(
            text("WITHOUT "),
            span(text("LIMITS"), span(".").withClass("signal-dot")).withClass("hero-title-accent")
          ).withClass("hero-title-line hero-title-end")
        ).withClass("hero-title"),
        p("A curated directory of frameworks, libraries and tools for building interfaces with Java. From desktop and web to mobile, terminal and beyond.")
          .withClass("hero-description"),
        div(
          h2("Find your next Java UI project").withClass("search-label").withId("search-heading"),
          rawHtml("""
        <pagefind-input placeholder="Search Java UI frameworks…"></pagefind-input>
        <pagefind-summary></pagefind-summary>
        <pagefind-results hide-sub-results>
          <script type="text/pagefind-template">
            <li class="pf-result pf-result-card project-card">
              <a class="pf-result-link" href="{{ url | safeUrl }}">
                <div class="project-card-content">
                  <img class="pf-result-image project-thumbnail" src="{{ meta.thumbnail }}" alt="">
                  <div class="project-name">{{ meta.title }} <span aria-hidden="true">↗</span></div>
                </div>
              </a>
            </li>
          </script>
        </pagefind-results>
        <div class="search-result-actions">
          <button class="search-result-action" type="button" data-search-clear>
            <i class="bi bi-x-circle"></i> Clear search
          </button>
          <button class="search-result-action" type="button" data-search-focus>
            <i class="bi bi-arrow-up"></i> Back to search
          </button>
        </div>
        <script>
          document.addEventListener("click", (event) => {
            if (!(event.target instanceof Element)) return;
            if (event.target.closest("[data-search-clear]")) {
              document.querySelector(".catalogue-search .pf-input-clear")?.click();
            } else if (event.target.closest("[data-search-focus]")) {
              const input = document.querySelector(".catalogue-search .pf-input");
              input?.scrollIntoView({ behavior: "smooth", block: "center" });
              input?.focus({ preventScroll: true });
            }
          });
        </script>
        """),
          div(
            a("Desktop UI").withHref("tag-desktop-ui.html"),
            a("Web UI").withHref("tag-web-ui.html"),
            a("Mobile UI").withHref("tag-mobile-ui.html"),
            a("Terminal UI").withHref("tag-terminal-ui.html")
          ).withClass("hero-quick-links"),
          noscript("Search requires JavaScript. Browse the complete project catalogue below.")
        ).withClass("catalogue-search").attr("aria-labelledby", "search-heading")
      ).withClass("hero-copy"),
      div(
        span().withClass("hero-shape hero-shape-orange").attr("aria-hidden", "true"),
        span().withClass("hero-shape hero-shape-blue").attr("aria-hidden", "true"),
        span().withClass("hero-shape hero-shape-yellow").attr("aria-hidden", "true"),
        heroPreview(heroFiles.get(1), markdownData, "preview-vaadin"),
        heroPreview(heroFiles.get(2), markdownData, "preview-mobile"),
        heroPreview(heroFiles.get(0), markdownData, "preview-javafx"),
        div(
          span("Real projects. Real possibilities.").withClass("hero-art-caption"),
          a("Explore the directory ↓").withHref("#category-section")
        ).withClass("hero-art-note")
      ).withClass("hero-art").attr("aria-label", "JavaFX, Vaadin and Codename One project previews")
    ).withClass("hero-section"),
    section(
      div(
        span("Find your platform").withClass("section-kicker"),
        h2("Four ways to build").withClass("section-title")
      ).withClass("section-heading"),
      div(each(Arrays.stream(categories).map(category ->
        categoryPanel(markdownData, category[0], category[1], category[2])
      ))).withClass("category-grid")
    ).withId("category-section").withClass("category-section"),
    section(
      div(
        div(
          span("A few worth a closer look").withClass("section-kicker section-kicker-light"),
          h2("Featured projects").withClass("section-title section-title-light")
        ),
        a("Browse all projects →").withHref("#browse-section").withClass("featured-all-link")
      ).withClass("featured-heading"),
      div(each(featuredFiles, fileName -> projectCard(fileName, markdownData.get(fileName))))
        .withClass("featured-grid")
    ).withId("featured-section").withClass("featured-section"),
    section(
      div(
        div(
          span("The whole Java UI landscape").withClass("section-kicker"),
          h2("Browse all projects").withClass("section-title").withId("browse-heading"),
          p("Filter the catalogue by platform, technology or project type.").withClass("catalogue-intro")
        ),
        a("Back to top ↑").withHref("#main-content").withClass("back-to-top")
      ).withClass("catalogue-heading"),
      div(
        a("All projects").withHref("#browse-section").withClass("catalogue-filter catalogue-filter-all"),
        each(uniqueTags, tag -> a(tag).withHref("tag-" + tagToSlug(tag) + ".html").withClass("catalogue-filter"))
      ).withClass("catalogue-filters").attr("aria-label", "Filter projects by category"),
      div(
        div(
          button(
            i().withClass("bi bi-sort-alpha-down"),
            text(" Alphabetical")
          )
            .withClass("sort-btn active")
            .attr("hx-get", "grid-alphabetical.html")
            .attr("hx-target", "#browse-section")
            .attr("hx-swap", "outerHTML transition:true"),
          button(
            i().withClass("bi bi-tags"),
            text(" Platform")
          )
            .withClass("sort-btn")
            .attr("hx-get", "grid-by-tag.html")
            .attr("hx-target", "#browse-section")
            .attr("hx-swap", "outerHTML transition:true"),
          button(
            i().withClass("bi bi-clock-history"),
            text(" Recently Added")
          )
            .withClass("sort-btn")
            .attr("hx-get", "grid-by-date.html")
            .attr("hx-target", "#browse-section")
            .attr("hx-swap", "outerHTML transition:true")
        ).withClass("sort-button-group").withId("sort-buttons")
      ).withClass("sort-controls-section"),
      gridAlphabeticalContent(markdownData)
    ).withId("catalogue-section").withClass("catalogue-section"),
    div(
      hr().withClass("tag-separator"),
      div(
        each(uniqueTags, tag -> {
          String tagSlug = tagToSlug(tag);
          String tagFileName = "tag-" + tagSlug + ".html";
          return a(tag)
            .withHref(tagFileName)
            .attr("hx-get", tagFileName)
            .attr("hx-target", "body")
            .attr("hx-swap", "innerHTML transition:true show:window:top")
            .attr("hx-push-url", "true")
            .withClass("tag-cloud-item");
        })
      ).withClass("tag-cloud")
    ).withClass("tag-cloud-section").withId("tags-section"),
    div(
      hr().withClass("about-separator"),
      div(
        h2("About This Guide"),
        p("This guide was created out of frustration with outdated and inaccurate resources about Java UI frameworks. I found an article that referenced archived libraries, included non-UI frameworks, and clearly hadn't been updated in years."),
        p("Rather than complain, I decided to build something better: an up-to-date overview of the UI options genuinely available to Java developers in 2026. This site covers desktop frameworks, web-based UIs written in Java, embedded browser approaches, terminal UIs, and everything in between."),
        p("The goal isn't to crown a \"best\" framework, but to lay out what's actually alive, maintained, and being used today. Each library is documented with its current status, Java version support, learning curve, and recent releases so you can make informed decisions."),
        p(
          text("This is a "),
          strong("community-driven reference"),
          text(". If you're building Java UIs, I'd love to hear about it:")
        ),
        ul(
          li("What are you using?"),
          li("What's surprisingly good?"),
          li("What should people stop recommending already?")
        ),
        p(
          text("Interested in more? Check out the original "),
          a("blog post")
            .withHref("https://robintegg.com/2026/02/08/java-ui-in-2026-the-complete-guide")
            .withTarget("_blank")
            .withRel("noopener noreferrer"),
          text(" or join the discussion on "),
          a("Reddit")
            .withHref("https://www.reddit.com/r/java/comments/1qzidbm/java_ui_in_2026_an_overview_of_current_frameworks/")
            .withTarget("_blank")
            .withRel("noopener noreferrer"),
          text(".")
        ),
        p("—Robin")
      ).withClass("about-content")
    ).withClass("about-section").withId("about-section")
  ).withId("main-content");
}

// SEO metadata record for page generation
record SeoMetadata(String title, String description, String image, String url, List<String> tags) {
  // Default metadata for index page
  static SeoMetadata index() {
    return new SeoMetadata(
      "Awesome Java UI - The Complete Guide to Java UI Frameworks",
      "A comprehensive guide to Java UI libraries and frameworks for desktop, web, terminal, and mobile development. Discover the best Java UI solutions for your next project.",
      "images/awesome-java-ui.png",
      "",
      List.of("Java", "UI", "Desktop", "Web", "Mobile", "Terminal")
    );
  }

  // Metadata for tag pages
  static SeoMetadata forTag(String tag) {
    return new SeoMetadata(
      tag + " - Awesome Java UI",
      "Explore Java UI libraries and frameworks tagged with " + tag + ". Find the best " + tag.toLowerCase() + " solutions for Java development.",
      "images/awesome-java-ui.png",
      "tag-" + tagToSlug(tag) + ".html",
      List.of("Java", "UI", tag)
    );
  }

  // Metadata for project pages
  static SeoMetadata forProject(Map<String, List<String>> data, String htmlFileName) {
    String name = data.getOrDefault("name", List.of("Unknown Project")).get(0);
    List<String> imageValues = data.get("image");
    String image = "images/awesome-java-ui.png";
    if (imageValues != null && !imageValues.isEmpty()) {
      String candidate = imageValues.get(0).trim();
      if (!candidate.isEmpty()) {
        image = candidate;
      }
    }
    List<String> projectTags = data.getOrDefault("tags", List.of());
    String status = data.getOrDefault("status", List.of("")).get(0);
    String javaVersion = data.getOrDefault("javaVersion", List.of("")).get(0);

    String description = name + " - " + status + ". ";
    if (!javaVersion.isEmpty()) {
      description += "Supports Java " + javaVersion + ". ";
    }
    if (!projectTags.isEmpty()) {
      description += "Categories: " + String.join(", ", projectTags) + ".";
    }

    return new SeoMetadata(
      name + " - Awesome Java UI",
      description,
      image,
      htmlFileName,
      projectTags
    );
  }

  private static String tagToSlug(String tag) {
    return tag.toLowerCase()
      .replaceAll("\\s+", "-")
      .replaceAll("[^a-z0-9-]", "");
  }
}

// Base URL for the site (used for absolute URLs in Open Graph tags)
static final String SITE_BASE_URL = "https://awesome-java-ui.com/";

static HtmlTag output(DomContent content, SeoMetadata seo) {
  String fullImageUrl = seo.image().startsWith("http") ? seo.image() : SITE_BASE_URL + seo.image();
  String fullPageUrl = SITE_BASE_URL + seo.url();
  String keywords = String.join(", ", seo.tags());

  return html(
    head(
      meta().withCharset("UTF-8"),
      meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),

      // Basic SEO meta tags
      title(seo.title()),
      meta().withName("description").withContent(seo.description()),
      meta().withName("keywords").withContent(keywords),
      meta().withName("author").withContent("Awesome Java UI Community"),
      meta().withName("robots").withContent("index, follow"),

      // Open Graph meta tags (Facebook, LinkedIn, etc.)
      meta().attr("property", "og:type").withContent("website"),
      meta().attr("property", "og:title").withContent(seo.title()),
      meta().attr("property", "og:description").withContent(seo.description()),
      meta().attr("property", "og:image").withContent(fullImageUrl),
      meta().attr("property", "og:url").withContent(fullPageUrl),
      meta().attr("property", "og:site_name").withContent("Awesome Java UI"),
      meta().attr("property", "og:locale").withContent("en_US"),

      // Twitter Card meta tags (also used by X, Mastodon, Bluesky)
      meta().withName("twitter:card").withContent("summary_large_image"),
      meta().withName("twitter:title").withContent(seo.title()),
      meta().withName("twitter:description").withContent(seo.description()),
      meta().withName("twitter:image").withContent(fullImageUrl),

      // Canonical URL
      link().withRel("canonical").withHref(fullPageUrl),

      // Fonts and stylesheets
      link().withRel("preconnect").withHref("https://fonts.googleapis.com"),
      link().withRel("preconnect").withHref("https://fonts.gstatic.com").attr("crossorigin", ""),
      link().withRel("stylesheet").withHref("css/styles.css"),
      link().withRel("stylesheet").withHref("pagefind/pagefind-component-ui.css"),
      script().withType("module").withSrc("pagefind/pagefind-component-ui.js"),
      link().withRel("stylesheet").withHref("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"),
      script().withSrc("https://unpkg.com/htmx.org@2.0.4"),
      rawHtml("<!-- 100% privacy-first analytics -->"),
      script().withSrc("https://scripts.simpleanalyticscdn.com/latest.js").attr("async", "")
    ),
    body(
      siteHeader(),
      content,
      siteFooter()
    )
  ).attr("lang", "en");
}

private static DomContent siteHeader() {
  return header(
    div(
      a(
        span("AWESOME").withClass("brand-line"),
        span("JAVA UI").withClass("brand-line brand-strong"),
        span(".").withClass("brand-dot")
      ).withHref("index.html").withClass("site-brand").attr("aria-label", "Awesome Java UI home"),
      nav(
        a("Projects").withHref("index.html#browse-section"),
        a("Categories").withHref("index.html#category-section"),
        a("About").withHref("index.html#about-section")
      ).withClass("site-nav").attr("aria-label", "Main navigation"),
      div(
        a("Search").withHref("index.html#search-heading").withClass("header-search"),
        a(
          i().withClass("bi bi-github").attr("aria-hidden", "true"),
          span("GitHub")
        ).withHref("https://github.com/teggr/awesome-java-ui")
          .withTarget("_blank")
          .withRel("noopener noreferrer")
          .withClass("header-github")
          .attr("aria-label", "Awesome Java UI on GitHub")
      ).withClass("header-actions")
    ).withClass("header-inner")
  ).withClass("site-header").attr("data-pagefind-ignore", "");
}

private static DomContent siteFooter() {
  return footer(
    div(
      div(
        span("A community-made guide to Java interfaces.").withClass("footer-tagline"),
        text("Built with "),
        a(
          i().withClass("bi bi-hammer"),
          text(" j2html")
        )
          .withHref("https://j2html.com/")
          .withTarget("_blank")
          .withRel("noopener noreferrer")
          .withClass("footer-link"),
        text(", "),
        a(
          i().withClass("bi bi-lightning-charge"),
          text(" htmx")
        )
          .withHref("https://htmx.org/")
          .withTarget("_blank")
          .withRel("noopener noreferrer")
          .withClass("footer-link"),
        text(" & "),
        a(
          i().withClass("bi bi-terminal"),
          text(" JBang")
        )
          .withHref("https://www.jbang.dev/")
          .withTarget("_blank")
          .withRel("noopener noreferrer")
          .withClass("footer-link")
      ).withClass("footer-content"),
      a("Contribute on GitHub →")
        .withHref("https://github.com/teggr/awesome-java-ui")
        .withTarget("_blank")
        .withRel("noopener noreferrer")
        .withClass("footer-contribute")
    ).withClass("footer-container")
  ).withClass("site-footer");
}
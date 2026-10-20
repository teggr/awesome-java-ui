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
        // Fix formatting: add newline after opening code tag for proper indentation
        html = html.replaceAll("(<code[^>]*>)([^ \\n])", "$1\n$2");

        // Write HTML to docs directory
        HtmlTag htmlTag = output(project(frontMatterVisitor.getData(), rawHtml(html)),
          SeoMetadata.forProject(frontMatterVisitor.getData(), path.getFileName().toString().replace(".md", ".html")));

        StringWriter writer = new StringWriter();
        writer.append(htmlTag.render(IndentedHtml.inMemory()));

        String finalHtml = writer.toString();

        String htmlFileName = path.getFileName().toString().replace(".md", ".html");

        markdownData.put( htmlFileName, frontMatterVisitor.getData() );

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
  String status = data.getOrDefault("status", List.of("Unknown Status")).get(0);
  String javaVersion = data.getOrDefault("javaVersion", List.of("Unknown Java Version")).get(0);
  String learningCurve = data.getOrDefault("learningCurve", List.of("Unknown Learning Curve")).get(0);
  String lastRelease = data.getOrDefault("lastRelease", List.of("Unknown Last Release")).get(0);
  String lastReleaseHref = data.getOrDefault("lastReleaseHref", List.of()).stream().findFirst().orElse("");
  String learnMoreText = data.getOrDefault("learnMoreText", List.of("Learn More")).get(0);
  String learnMoreHref = data.getOrDefault("learnMoreHref", List.of("#")).get(0);
  String image = data.getOrDefault("image", List.of("https://via.placeholder.com/150")).get(0);
  List<String> tags = data.getOrDefault("tags", List.of());

  return div(
    div(
      a("← Back")
        .withHref("index.html")
        .attr("onclick", "history.back(); return false;") // just navigate back in history for better UX, instead of reloading the page
//        .attr("hx-get", "index.html")
//        .attr("hx-target", "body")
//        .attr("hx-swap", "innerHTML transition:true show:window:top")
//        .attr("hx-push-url", "true")
        .withClass("back-link")
    ),
    each(
      h1(name),
      img().withSrc(image).withAlt(name),
      p("Status: " + status),
      p("Java Version: " + javaVersion),
      p("Learning Curve: " + learningCurve),
      p(
        text("Last Release: "),
        lastReleaseHref.isBlank()
          ? text(lastRelease)
          : a(lastRelease)
              .withHref(lastReleaseHref)
              .withTarget("_blank")
              .attr("rel", "noopener noreferrer")
      ),
      a(
        text(learnMoreText),
        i().withClass("bi bi-box-arrow-up-right")
      ).withHref(learnMoreHref).withTarget("_blank"),
      div(
        each(tags, tag -> {
          String tagSlug = tagToSlug(tag);
          String tagFileName = "tag-" + tagSlug + ".html";
          return a(tag)
            .withHref(tagFileName)
            .attr("hx-get", tagFileName)
            .attr("hx-target", "body")
            .attr("hx-swap", "innerHTML transition:true show:window:top")
            .attr("hx-push-url", "true")
            .withClass("tag");
        })
      ).withClass("tags"),
      content
    )
  ).withId("main-content");

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
  
  return div(
    div(
        a("← Back")
        .withHref("index.html")
        .attr("onclick", "history.back(); return false;") // just navigate back in history for better UX, instead of reloading the page
//        .attr("hx-get", "index.html")
//        .attr("hx-target", "body")
//        .attr("hx-swap", "innerHTML transition:true show:window:top")
//        .attr("hx-push-url", "true")
        .withClass("back-link")
    ),
    h1("Projects tagged: " + tag),
    div(
      each(filteredProjects.entrySet(), entry -> {
        String htmlFileName = entry.getKey();
        String projectName = entry.getValue().getOrDefault("name", List.of("ProjectX")).get(0);
        String imageUrl = entry.getValue().getOrDefault("image", List.of("https://via.placeholder.com/150")).get(0);
        String thumbnailUrl = getThumbnailUrl(imageUrl);
        return a(
          div(
            img().withSrc(thumbnailUrl).withAlt(projectName).withClass("project-thumbnail"),
            div(projectName).withClass("project-name")
          ).withClass("project-card-content")
        )
          .withHref(htmlFileName)
          .attr("hx-get", htmlFileName)
          .attr("hx-target", "body")
          .attr("hx-swap", "innerHTML transition:true show:window:top")
          .attr("hx-push-url", "true")
          .withClass("project-card");
      })
    ).withClass("project-list")
  ).withId("main-content");
}

// Grid content for initial page render (no OOB button)
static DomContent gridAlphabeticalContent(Map<String, Map<String, List<String>>> markdownData) {
  return div(
    each( markdownData.entrySet(), entry -> {
      String htmlFileName = entry.getKey();
      String projectName = entry.getValue().getOrDefault("name", List.of("ProjectX")).get(0);
      String imageUrl = entry.getValue().getOrDefault("image", List.of("https://via.placeholder.com/150")).get(0);
      String thumbnailUrl = getThumbnailUrl(imageUrl);
      return a(
        div(
          img().withSrc(thumbnailUrl).withAlt(projectName).withClass("project-thumbnail"),
          div(projectName).withClass("project-name")
        ).withClass("project-card-content")
      )
        .withHref(htmlFileName)
        .attr("hx-get", htmlFileName)
        .attr("hx-target", "body")
        .attr("hx-swap", "innerHTML transition:true show:window:top")
        .attr("hx-push-url", "true")
        .withClass("project-card");
    })
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
          each(filteredProjects.entrySet(), entry -> {
            String htmlFileName = entry.getKey();
            String projectName = entry.getValue().getOrDefault("name", List.of("ProjectX")).get(0);
            String imageUrl = entry.getValue().getOrDefault("image", List.of("https://via.placeholder.com/150")).get(0);
            String thumbnailUrl = getThumbnailUrl(imageUrl);
            return a(
              div(
                img().withSrc(thumbnailUrl).withAlt(projectName).withClass("project-thumbnail"),
                div(projectName).withClass("project-name")
              ).withClass("project-card-content")
            )
              .withHref(htmlFileName)
              .attr("hx-get", htmlFileName)
              .attr("hx-target", "body")
              .attr("hx-swap", "innerHTML transition:true show:window:top")
              .attr("hx-push-url", "true")
              .withClass("project-card");
          })
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
    each(sortedData.entrySet(), entry -> {
      String htmlFileName = entry.getKey();
      String projectName = entry.getValue().getOrDefault("name", List.of("ProjectX")).get(0);
      String imageUrl = entry.getValue().getOrDefault("image", List.of("https://via.placeholder.com/150")).get(0);
      String thumbnailUrl = getThumbnailUrl(imageUrl);
      return a(
        div(
          img().withSrc(thumbnailUrl).withAlt(projectName).withClass("project-thumbnail"),
          div(projectName).withClass("project-name")
        ).withClass("project-card-content")
      )
        .withHref(htmlFileName)
        .attr("hx-get", htmlFileName)
        .attr("hx-target", "body")
        .attr("hx-swap", "innerHTML transition:true show:window:top")
        .attr("hx-push-url", "true")
        .withClass("project-card");
    })
  ).withClass("project-list").withId("browse-section");
}

// Partial response for htmx: date-sorted grid + OOB button swap
static DomContent gridByDate(Map<String, Map<String, List<String>>> markdownData) {
  return each(
    gridByDateContent(markdownData),
    sortButtons("date")
  );
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
  // Collect unique tags
  Set<String> uniqueTags = collectUniqueTags(markdownData);

  return div(
    // Hero Section with split layout
    div(
      // Left column - Title and CTA
      div(
        h1("Awesome Java UI").withClass("hero-title"),
        a(
          i().withClass("bi bi-github"),
          text(" Contribute on GitHub")
        )
          .withHref("https://github.com/teggr/java-ui-the-complete-guide")
          .withTarget("_blank")
          .withRel("noopener noreferrer")
          .withClass("github-cta")
      ).withClass("hero-left"),
      // Right column - Description paragraphs
      div(
        p("Welcome to Awesome Java UI! This site provides an overview of the latest and greatest Java UI projects, frameworks and libraries, along with their status, Java version compatibility, learning curve, last release date, and more. Explore the projects below to find the right Java UI solution for your needs."),
        p("This is a community-driven resource, built by Java developers for Java developers. Whether you're discovering a new framework, sharing your expertise, or helping others navigate the Java UI landscape - your contributions make this guide better for everyone. Join us in building the most comprehensive resource for Java UI development!")
      ).withClass("hero-right")
    ).withClass("hero-section"),
    // Sort controls section - above the project grid
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
    gridAlphabeticalContent(markdownData),
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
      link().withRel("stylesheet").withHref("https://fonts.googleapis.com/css2?family=Poppins:wght@700;800&display=swap"),
      link().withRel("stylesheet").withHref("css/styles.css"),
      link().withRel("stylesheet").withHref("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"),
      script().withSrc("https://unpkg.com/htmx.org@2.0.4"),
      rawHtml("<!-- 100% privacy-first analytics -->"),
      script().withSrc("https://scripts.simpleanalyticscdn.com/latest.js").attr("async", "")
    ),
    body(
      content,
      siteFooter()
    )
  ).attr("lang", "en");
}

private static DomContent siteFooter() {
  return footer(
    div(
      div(
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
      ).withClass("footer-content")
    ).withClass("footer-container")
  ).withClass("site-footer");
}
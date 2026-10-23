# Awesome Java UI

Guide to the Java UI landscape: desktop, web, terminal and mobile

Visit [https://awesome-java-ui.com](https://awesome-java-ui.com)

## Quick Start

### Option 1: GitHub Codespaces or VS Code DevContainer
The easiest way to get started is with our pre-configured development environment:

1. Open this repository in GitHub Codespaces or VS Code with DevContainers
2. The environment will automatically set up Java 25 and JBang
3. Ensure Node.js 22+ and npm are installed (they are additional search-build prerequisites)
4. Run `jbang run build.java` to generate the site, then `npm ci` and
   `npm run search:index` to build the Pagefind search index
5. Run `npm run preview` and open `http://localhost:1414` to view the site

### Option 2: Local Development
**Prerequisites:**
- Java 25+
- JBang
- Node.js 22+ and npm (for the pinned Pagefind CLI)

**Build and serve the site:**

```bash
# Generate thumbnails for images (run this after adding new images)
jbang GenerateThumbnails.java

# Generate the site HTML
jbang run build.java

# Install the locked Pagefind dependency
npm ci

# Index the generated HTML and write browser assets to _site/pagefind/
npm run search:index

# Validate the generated search output
npm test

# Serve locally (open http://localhost:1414)
npm run preview
```

The site will be available at `http://localhost:1414`. Use HTTP rather than opening
`_site/index.html` directly: search loads its index with browser requests.

### Static search

[Pagefind](https://pagefind.app/) **1.5.2**, pinned in `package.json` and
`package-lock.json`, indexes the generated HTML after JBang finishes. JBang
generates the site; Node/npm only run Pagefind and its validation tests. No
search service, global npm installation, or separate catalogue data is needed.
Run `npm run search:index` after `jbang run build.java`, and reindex after
content changes. For a clean build, remove `_site/` first so deleted projects
do not linger.

Only individual project pages are indexed, using `data-pagefind-body`.
Back navigation, footers, catalogue copies, tag pages and HTMX fragments are
excluded. Search includes rendered descriptions, tags, status, Java version,
learning curve, release text and code. Link destinations, image paths and
unrendered metadata such as `dateAdded` are not searchable text.

The homepage uses Pagefind's inline input, summary and results components, with
default relevance, excerpts, a Clear button, keyboard navigation, and loading,
empty and error states. The search index loads on interaction, not on initial
catalogue rendering; matching index chunks and result excerpts load on demand.
The components reconnect automatically after HTMX navigation without adding
custom HTMX handlers. A history restoration via HTMX resets the search query;
ordinary browser back/forward caching may retain it. Sorting continues to affect
the catalogue below the independent search results.

**Validation:** `npm test` checks the generated page count, indexing markers,
project-title metadata and deployed search assets; run it after
`jbang run build.java` and `npm run search:index`.
For browser smoke tests, try `JavaFX` (project name), `hardware accelerated`
(description), `Terminal UI` (tag), `Production-ready` (status), `17` (Java version),
`HelloJavaFX` (code), and `zzzznonexistentframework` (empty state). Check Clear and
Escape, keyboard navigation to result links, all three sort controls, tag
navigation and browser Back, including at a narrow mobile viewport.
Search is word-based, not literal source-code or structured-version matching.
Default ranking may put a related project ahead of an exact project-name match.

GitHub Actions runs `jbang run build.java`, then `npm ci`, `npm run search:index`,
and `npm test`, before uploading all of `_site/`, including `pagefind/`, for
static deployment. The homepage hero and featured projects rotate by UTC date
across eligible projects in their platform categories. A scheduled workflow
rebuilds and deploys the site daily at 06:17 UTC; local and manual builds use
the same date-based selection.
Neither `_site/` nor `node_modules/` is committed.

### Scheduled release audit

The `audit-project-releases` GitHub Agentic Workflow runs weekly on Mondays and can also be started manually from the Actions tab. It compares each project's `lastRelease` value with the verified upstream source in `lastReleaseHref` (GitHub Releases/Tags or Maven Central), then opens a pull request only when it confirms a newer stable version. Entries without a supported source link are skipped. To compile or validate the workflow locally, install the [GitHub Agentic Workflows CLI](https://github.github.com/gh-aw/) and run `gh aw compile`.

**About Thumbnails:**
The site uses automatically generated thumbnails for faster page loading. When you add a new image to the `images/` directory:
1. Run `jbang GenerateThumbnails.java` to generate thumbnails
2. Thumbnails are created as `thumbnail-{name}.png` with max height of 540px
3. The script intelligently skips unchanged images using hash tracking
4. The build process automatically uses thumbnails with fallback to original images

## Contributing

We welcome contributions! There are several ways you can help:

### Option 1: Submit a Pull Request
If you're comfortable with Git and markdown:
1. Fork this repository
2. Create a new `.md` file for the Java UI library (e.g., `my-framework.md`)
3. Follow the markdown template structure (see existing files like `htmx.md`)
4. Submit a pull request

### Option 2: Create a GitHub Issue
If you'd like to suggest a library but don't have time for a full PR:
1. [Create a new issue](https://github.com/YOUR_USERNAME/java-ui-the-complete-guide/issues/new)
2. Provide at minimum:
   - **Project name**
   - **Project URL**

### Option 3: Create a Detailed Issue
If you really want to help, provide the full markdown content in the issue using this template:

```markdown
---
name: Framework Name
status: Production-ready / Active / Archived / Beta
javaVersion: Java 17+
learningCurve: Easy / Moderate / Steep
lastRelease: vX.X.X (YYYY)
lastReleaseHref: https://github.com/example/project/releases
learnMoreText: Official Website
learnMoreHref: https://example.com
image: images/ui-framework-name.png
tags:
    - Desktop UI / Web UI / Terminal UI / Mobile UI
    - Web Framework / UI Components / etc.
---

Brief description of the framework (2-3 paragraphs explaining what it is, 
its key features, and why it's relevant to Java developers).

## Code Example

\```java
// Minimal working example demonstrating the framework
public class Example {
    public static void main(String[] args) {
        // Your example code here
    }
}
\```
```

**Required metadata fields:**
- `name`: Display name
- `status`: Current project status
- `javaVersion`: Supported Java versions
- `learningCurve`: Difficulty level
- `lastRelease`: Latest version and year
- `lastReleaseHref`: Optional link to the verified GitHub releases/tags page or Maven repository used as the release source
- `learnMoreText` / `learnMoreHref`: Link to documentation
- `image`: Screenshot/logo filename
- `tags`: Categories (Desktop UI, Web Framework, Terminal UI, etc.)

All contributions are appreciated! 🎉

### Automated candidate discovery

The **Discover Java UI candidates** GitHub Agentic Workflow runs weekly on Monday around 13:00 UTC and can also be started manually from the Actions tab. Its Java 25/JBang collector searches GitHub and Maven Central; the agent checks likely matches and creates a new dated issue with the `candidate-discovery` label only when it finds plausible candidates. Each issue title includes the workflow run number.

These are unverified leads, not automatic additions. Please check that a candidate is a Java UI library or framework and is not already listed before opening a contribution. Reddit is not currently used as a source.

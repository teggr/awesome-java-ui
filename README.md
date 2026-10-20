# Awesome Java UI

Guide to the Java UI landscape: desktop, web, terminal and mobile

Visit [https://awesome-java-ui.com](https://awesome-java-ui.com)

## Quick Start

### Option 1: GitHub Codespaces or VS Code DevContainer
The easiest way to get started is with our pre-configured development environment:

1. Open this repository in GitHub Codespaces or VS Code with DevContainers
2. The environment will automatically set up Java 25 and JBang
3. Run `jbang run build.java` to build the site
4. Open `_site/index.html` in a browser to view the site

### Option 2: Local Development
**Prerequisites:**
- Java 25+
- JBang

**Build and serve the site:**

```bash
# Generate thumbnails for images (run this after adding new images)
jbang GenerateThumbnails.java

# Build the site (generates HTML in _site/ directory)
jbang run build.java

# Serve locally (open http://localhost:8000)
cd _site
"%JAVA_HOME%\bin\jwebserver"
```

The site will be available at `http://localhost:8000`

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

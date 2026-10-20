# Copilot Instructions for Awesome Java UI

This repository is a comprehensive guide to the Java UI landscape (desktop, web, terminal, and mobile) presented as a static website.

## Build and Development Commands

**Build the site:**
```bash
jbang run build.java
```
This generates HTML files in the `_site/` directory by:
1. Parsing all `.md` files (except README.md) in the root directory
2. Extracting YAML front matter and converting markdown to HTML
3. Wrapping content in templates using j2html
4. Generating an index page with all projects
5. Copying CSS files to the output directory


**Requirements:**
- Java 25+ (for building with JBang)
- JBang (for running build.java)

## Architecture Overview

### Static Site Generation
- **Input:** Markdown files in the project root (one per Java UI library/framework)
- **Build Script:** `build.java` - A JBang script using j2html and CommonMark
- **Output:** Static HTML files in `_site/` directory
- **Frontend:** HTMX for client-side navigation with transitions

### Content Structure
Each markdown file represents a Java UI framework/library with:
- **YAML front matter** containing structured metadata (name, status, javaVersion, learningCurve, lastRelease, learnMoreHref, image, tags)
- **Markdown content** with descriptions, code examples, and features

The front matter schema:
- `name`: Display name of the framework
- `status`: Production status (e.g., "Production-ready", "Active", "Archived")
- `javaVersion`: Supported Java versions
- `learningCurve`: Difficulty level (e.g., "Low", "Moderate", "Steep")
- `lastRelease`: Release information
- `lastReleaseHref`: Optional link to the verified GitHub releases/tags page or Maven repository used as the release source; do not guess the URL
- `learnMoreText` / `learnMoreHref`: Link to official documentation
- `image`: URL to logo/screenshot
- `tags`: Array of categories (e.g., "Desktop UI", "Web Framework", "Terminal UI")

### Template System
- `project()` function: Generates individual project pages with back navigation
- `indexPage()` function: Generates the homepage with all projects in a grid
- `output()` function: Wraps content in HTML boilerplate with HTMX

## Key Conventions

### Adding New UI Libraries
1. Create a new `.md` file in the root directory named after the library (e.g., `my-framework.md`)
2. Include complete YAML front matter with all required fields
3. Write descriptive content and include a code example section
4. Run the build script - the page will automatically appear on the index

### Markdown File Naming
- Use lowercase with hyphens (kebab-case)
- File names become HTML filenames directly (e.g., `javafx.md` → `javafx.html`)
- Avoid spaces or special characters

### Code Examples
- Include a `## Code Example` section in each markdown file
- Use Java syntax highlighting with triple backtick fences
- Keep examples minimal and self-contained to demonstrate the framework's basic usage

### CI/CD
- GitHub Actions workflow in `.github/workflows/build-site.yml`
- Automatically builds and deploys to GitHub Pages on push to main
- Uses JBang to run the build script in the CI environment

### Copilot Java 25 Workflow
- GitHub Actions workflow in `.github/workflows/copilot-setup-steps.yml`
- Ensures Java 25 environment for Copilot agents on pull requests
- Includes JBang setup with caching for improved performance
- Validates the build process with Java 25
- Can be manually triggered via workflow_dispatch

### Development Environment
- DevContainer configuration in `.devcontainer/devcontainer.json`
- Pre-configured with Java 25 for GitHub Codespaces and VS Code
- Includes GitHub Copilot extensions and Java development tools
- Automatically sets up JBang on container creation
- Provides consistent development environment across all contributors

### Local Development
- Output directory (`_site/`) is gitignored
- Rebuild the site locally after markdown changes to preview
- Open `_site/index.html` in a browser to view the site

## Web Site Styles

- CSV

001524,15616d,ffecd1,ff7d00,78290f

- With #

#001524, #15616d, #ffecd1, #ff7d00, #78290f

- Array

["001524","15616d","ffecd1","ff7d00","78290f"]

- Object

{"Ink Black":"001524","Stormy Teal":"15616d","Papaya Whip":"ffecd1","Vivid Tangerine":"ff7d00","Brandy":"78290f"}

- Extended Array

[{"name":"Ink Black","hex":"001524","rgb":[0,21,36],"cmyk":[100,42,0,86],"hsb":[205,100,14],"hsl":[205,100,7],"lab":[6,-2,-13]},{"name":"Stormy Teal","hex":"15616d","rgb":[21,97,109],"cmyk":[81,11,0,57],"hsb":[188,81,43],"hsl":[188,68,25],"lab":[38,-18,-13]},{"name":"Papaya Whip","hex":"ffecd1","rgb":[255,236,209],"cmyk":[0,7,18,0],"hsb":[35,18,100],"hsl":[35,100,91],"lab":[94,2,15]},{"name":"Vivid Tangerine","hex":"ff7d00","rgb":[255,125,0],"cmyk":[0,51,100,0],"hsb":[29,100,100],"hsl":[29,100,50],"lab":[66,44,74]},{"name":"Brandy","hex":"78290f","rgb":[120,41,15],"cmyk":[0,66,88,53],"hsb":[15,88,47],"hsl":[15,78,26],"lab":[28,33,34]}]

- XML

<palette>
  <color name="Ink Black" hex="001524" r="0" g="21" b="36" />
  <color name="Stormy Teal" hex="15616d" r="21" g="97" b="109" />
  <color name="Papaya Whip" hex="ffecd1" r="255" g="236" b="209" />
  <color name="Vivid Tangerine" hex="ff7d00" r="255" g="125" b="0" />
  <color name="Brandy" hex="78290f" r="120" g="41" b="15" />
</palette>

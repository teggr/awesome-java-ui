---
description: Create a new markdown documentation file for a Java UI library project following the defined template and workflow. This includes researching the library, writing a description, creating a code example, acquiring an image, and ensuring all metadata is accurate.
---

# Java UI Library Documentation Skill

This document describes the workflow and templates for creating documentation entries for Java UI libraries in this project.

## Overview

This project documents Java UI libraries across all categories (Desktop, Web, Terminal, Mobile, Build Tools, etc.) as individual markdown files that are compiled into a static website using JBang and j2html.

## Markdown File Structure

Each library is documented in a markdown file at the project root following this template:

### YAML Front Matter Schema

```yaml
---
name: Library Name                    # Display name
status: Production-ready              # Production status (Production-ready, Active, Legacy, Archived, Experimental)
javaVersion: 11+                      # Java version support (e.g., "8+", "11+", "17+ recommended")
learningCurve: Moderate               # Difficulty (Easy, Easy-Moderate, Moderate, Moderate-Steep, Steep)
lastRelease: v1.2.3 (January 2026)   # Latest version with date or "Active"/"Ongoing"
lastReleaseHref: https://github.com/owner/repository/releases # Verified GitHub releases/tags or Maven artifact source
learnMoreText: Official Website       # Link text for documentation
learnMoreHref: https://example.com/   # URL to official docs
image: images/ui-libraryname.png      # Image path - MUST use relative path (images/ui-*.png), NOT absolute (/images/)
tags:                                 # Array of category tags
    - Desktop UI
    - Web UI
---
```

When `lastRelease` is a specific version, include `lastReleaseHref` only when the GitHub releases/tags page or Maven repository page has been verified as the source. Do not infer or guess this URL.

### Description Section

Immediately after front matter, write a 100-200 word description covering:

1. **Purpose/Overview** - What the library does and its primary use case
2. **Key Features** - Notable capabilities, architecture, or design philosophy
3. **Ecosystem Position** - How it relates to other libraries, its maturity, community
4. **Technical Details** - Noteworthy technical aspects (performance, dependencies, etc.)
5. **Ideal Use Cases** - Who should use this and when

**Tone**: Professional, informative, balanced. Acknowledge both strengths and limitations.

### Code Example Section

```markdown
## Code Example

\```java
// Working, realistic example demonstrating core value
// Include necessary imports
// Show typical API usage
// Comment complex parts
// Keep it under 40 lines
\```
```

## Tag Categories

Standard tags (add new categories as needed):

- **Desktop UI** - Native desktop frameworks (Swing, JavaFX, etc.)
- **Web UI** - Browser-based interfaces
- **Web Framework** - Backend web frameworks
- **Template Engine** - Server-side templating
- **Terminal UI** - Command-line/TUI interfaces
- **Mobile** - Mobile platform support
- **Cloud-Native** - Cloud/Kubernetes-focused
- **Microservices** - Microservice frameworks
- **Game Development** - Game engines/frameworks
- **Build Tools** - Maven/Gradle plugins, packaging tools
- **UI Components** - Component libraries
- **Compiler/Transpiler** - Code transformation tools
- **Frontend Library** - Client-side JavaScript libraries
- **Legacy** - Maintained but not recommended for new projects

## Web Research Workflow

### 1. Version and Release Information

```bash
# GitHub releases
curl -s https://api.github.com/repos/{owner}/{repo}/releases/latest | grep tag_name

# Check Maven Central
https://central.sonatype.com/artifact/{group}/{artifact}

# Check project website for version info
```

### 2. Validation and Documentation

- Visit official website/documentation
- Check GitHub repository activity (commits, issues, releases)
- Verify Java version compatibility from docs or build files
- Note production status indicators (alpha/beta warnings, stability claims)
- Assess learning curve from documentation quality and API complexity

### 3. URL Validation

If original URL is dead:
1. Try archived version: `https://web.archive.org/`
2. Check GitHub for official redirects
3. Use successor project URL if framework was renamed
4. Add `urlValidation` field to JSON: `"urlValidation": "original-invalid, using: <alternative>"`

### 4. Image Acquisition Strategy

Priority order:

1. **Website Screenshot** (~800x600px)
   - Use browser dev tools or screenshot tool
   - Capture hero section or example UI
   - Save as `images/ui-{kebab-name}.png`

2. **Website Hero/Jumbotron Image**
   - Download featured image from homepage
   - Official brand imagery

3. **Official Logo**
   - From GitHub repository (`/logo.png`, `/docs/logo.png`)
   - From official website
   - High-resolution preferred (>200px width)

```bash
# Download image examples
curl -L "https://example.com/logo.png" -o images/ui-libraryname.png

# From GitHub raw
curl -L "https://github.com/{owner}/{repo}/raw/main/logo.png" -o images/ui-libraryname.png
```

**Important: Image Path Format**

Always use **relative paths** in the `image:` field:
- ✅ Correct: `image: images/ui-libraryname.png`
- ❌ Wrong: `image: /images/ui-libraryname.png`

Absolute paths starting with `/` break when the site is deployed to GitHub Pages under a subdirectory. Relative paths work correctly in all deployment scenarios.

## File Naming Conventions

- Markdown files: `kebab-case-name.md` (e.g., `javafx-maven-plugin.md`)
- Image files: `ui-{kebab-case-name}.png` (e.g., `ui-javafx-maven-plugin.png`)
- Kebab case: lowercase with hyphens, no spaces or special characters

## Creating a New Entry

### Step-by-Step Process

1. **Research the Library**
   ```bash
   # Gather information
   - Official website and documentation
   - GitHub repository (stars, activity, latest release)
   - Maven Central or package manager
   - Community resources (blogs, tutorials)
   ```

2. **Determine Metadata**
   - Status: Check release activity, maintenance status, production use
   - Java Version: From docs or build files (pom.xml, build.gradle)
   - Learning Curve: Based on API complexity and documentation quality
   - Last Release: Check GitHub releases or changelog

3. **Write Description**
   - Start with one-sentence overview
   - Expand to 2-3 paragraphs covering purpose, features, ecosystem
   - Include technical specifics (architecture, performance characteristics)
   - End with use case recommendations

4. **Create Code Example**
   - Show "Hello World" or simplest meaningful example
   - Include all necessary imports
   - Use idiomatic API patterns
   - Add comments for clarity
   - Ensure code compiles/makes sense

5. **Acquire Image**
   ```bash
   cd images/
   # Try each method in priority order
   curl -L "<image-url>" -o ui-libraryname.png
   ```

6. **Create Markdown File**
   ```bash
   # At project root
   cat > libraryname.md << 'EOF'
   ---
   name: Library Name
   ...front matter...
   ---
   
   Description paragraph...
   
   ## Code Example
   
   \```java
   // Example code
   \```
   EOF
   ```

7. **Update Progress Tracking** (if using suggested_libraries.json)
   ```json
   {
     "name": "Library Name",
     "processed": true,
     "markdownFile": "libraryname.md",
     "notes": "Version info, special notes"
   }
   ```

8. **Build and Verify**
   ```bash
   jbang run build.java
   # Check _site/ output
   # Verify image loads
   # Verify formatting
   ```

## Special Cases

### JSP (Jakarta Pages)
- Use Jakarta EE specification as source: https://jakarta.ee/specifications/pages/
- Note legacy status but ongoing maintenance
- Reference Jakarta namespace in examples

### Build Tools / Plugins
- Focus on configuration examples (pom.xml, build.gradle)
- Show actual build command usage
- Mention IDE integration if applicable

### Archived/Unmaintained Projects
- Clearly mark status as "Archived" or "No Longer Maintained"
- Note last activity date
- Suggest active alternatives
- Include historical context for why it mattered

### Commercial/Freemium Products
- Clearly indicate licensing model
- Note free tier limitations if applicable
- Mention enterprise features separately

## Reusable Prompt Pattern

When asking an AI to create a new entry:

```
Create a markdown documentation file for [Library Name] following this template:

Library: [Name]
Official URL: [URL]
Category: [Desktop UI / Web Framework / etc.]

Requirements:
1. Research current version and release date from GitHub/Maven
2. Write 100-200 word description covering purpose, features, ecosystem position, use cases
3. Create realistic "Hello World" code example with imports
4. Use appropriate tags from: Desktop UI, Web UI, Web Framework, Template Engine, etc.
5. Assess learning curve (Easy/Moderate/Steep) based on API complexity
6. Download logo/image and save to images/ui-[kebab-name].png
7. Use YAML front matter schema defined above
8. IMPORTANT: Use relative image path (images/ui-*.png) NOT absolute path (/images/ui-*.png)

Output should be a complete markdown file ready to commit.
```

## Build Integration

The build script (`build.java`) automatically:
- Processes all `.md` files in project root (except README.md)
- Extracts YAML front matter
- Converts markdown to HTML
- Generates individual pages with navigation
- Creates index page listing all libraries
- Copies `css/` and `images/` directories to `_site/`

No manual build configuration needed for new files—just create the markdown file and run build.

## Quality Checklist

Before committing a new library entry:

- [ ] YAML front matter is complete and valid
- [ ] Description is 100-200 words
- [ ] Description is factual and balanced
- [ ] Code example compiles/makes sense
- [ ] Code example includes imports
- [ ] Image file exists in images/ directory
- [ ] Image displays correctly (not corrupt)
- [ ] Image path uses relative format (images/ui-*.png, NOT /images/ui-*.png)
- [ ] Filename uses kebab-case
- [ ] Tags are appropriate and consistent
- [ ] URL is validated and accessible
- [ ] Build succeeds with new file
- [ ] HTML output looks correct

## Additional Resources

- Project repository: https://github.com/teggr/java-ui-the-complete-guide
- Build script: `build.java` (JBang-based static site generator)
- Template examples: `javafx.md`, `jcef.md`, `vaadin.md`, `lanterna.md`

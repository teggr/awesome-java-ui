---
name: Maven Tailwind Plugin
status: Active, Community Project
javaVersion: 11+
learningCurve: Easy
lastRelease: v1.2.0 (July 2026)
lastReleaseHref: https://github.com/4ndreiDev/maven-tailwind-plugin/releases
learnMoreText: Maven Tailwind Plugin GitHub
learnMoreHref: https://github.com/4ndreiDev/maven-tailwind-plugin
image: images/ui-maven-tailwind-plugin.png
tags:
    - Build Tools
    - Web UI
dateAdded: 2026-02-13
---

The Maven Tailwind Plugin integrates Tailwind CSS into Maven-based Java web applications without requiring Node.js or npm in your build pipeline. Tailwind CSS is a popular utility-first CSS framework that has become the standard for modern web development, but it typically requires Node.js tooling for compilation. This plugin downloads the standalone Tailwind CSS CLI binary and executes it during the Maven build process, scanning your HTML/template files for Tailwind classes and generating the minimal CSS file needed. This is particularly useful for Java server-side rendering frameworks like Spring Boot with Thymeleaf, Vaadin, JTE, or other template engines where you want Tailwind's utility classes without introducing Node.js dependencies. The plugin handles Tailwind configuration, watches for file changes during development, and produces optimized production builds with purged unused CSS. For Java teams building web UIs with server-side templates who want modern CSS tooling without JavaScript build complexity, this plugin provides a clean Maven-native solution.

## Code Example

```xml
<!-- pom.xml - Add Tailwind CSS to Java web app -->
<build>
    <plugins>
        <plugin>
            <groupId>com.github.4ndreidev</groupId>
            <artifactId>maven-tailwind-plugin</artifactId>
            <version>1.1.0</version>
            <configuration>
                <input>src/main/resources/static/css/input.css</input>
                <output>target/classes/static/css/output.css</output>
                <watch>true</watch> <!-- Enable in dev mode -->
                <minify>true</minify> <!-- For production -->
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>build</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

<!-- input.css: -->
<!-- @tailwind base; -->
<!-- @tailwind components; -->
<!-- @tailwind utilities; -->

<!-- Use in templates: <div class="bg-blue-500 p-4">Tailwind!</div> -->
```

---
name: Badass JLink Plugin
status: Production-ready, Active
javaVersion: 11+
learningCurve: Moderate
lastRelease: v4.1.1
lastReleaseHref: https://github.com/beryx/badass-jlink-plugin/releases
learnMoreText: Badass JLink Plugin Documentation
learnMoreHref: https://badass-jlink-plugin.beryx.org/releases/latest/
image: images/ui-badass-jlink-plugin.png
tags:
    - Build Tools
    - Desktop UI
dateAdded: 2026-02-18
---

The Badass JLink Plugin is a powerful Gradle plugin that simplifies the creation of custom runtime images for modular Java applications using the jlink tool. It handles the complexity of working with modular and automatic modules, making it straightforward to create minimal, optimized runtime images that include only the JDK modules your application actually needs. The plugin also integrates with jpackage to generate native installers (DMG, PKG, MSI, EXE, DEB, RPM) for Windows, macOS, and Linux platforms. Unlike the JavaFX Maven Plugin which focuses on Maven builds, Badass JLink brings the same level of sophistication to Gradle-based projects. The plugin automatically analyzes your application's module dependencies, handles both modular and non-modular dependencies gracefully, and provides extensive configuration options for launcher scripts, JVM arguments, compression levels, and file associations. It's particularly well-suited for JavaFX applications but works with any modular Java application. Maintained by the Beryx team, it's widely adopted in the Java desktop community and represents the de facto standard for creating custom runtime images in Gradle builds.

## Code Example

```gradle
// build.gradle
plugins {
    id 'org.beryx.jlink' version '3.1.0'
}

jlink {
    options = ['--strip-debug', '--compress', '2', '--no-header-files', '--no-man-pages']
    
    launcher {
        name = 'myapp'
        jvmArgs = ['-Dlog4j.configurationFile=./log4j2.xml']
    }
    
    jpackage {
        installerType = 'msi'  // or 'dmg', 'pkg', 'exe', 'deb', 'rpm'
        installerName = 'MyApplication'
        appVersion = '1.0.0'
        
        // Platform-specific options
        imageOptions = ['--icon', 'src/main/resources/icon.png']
        installerOptions = [
            '--vendor', 'My Company',
            '--copyright', 'Copyright 2026'
        ]
    }
}

// Build custom runtime image: ./gradlew jlink
// Create installer: ./gradlew jpackage
```

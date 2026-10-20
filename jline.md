---
name: JLine
status: Production-ready, Very Active
javaVersion: 11+ (JLine 4.x), 8+ (JLine 3.x)
learningCurve: Easy-Moderate
lastRelease: v4.4.8 (October 2026)
lastReleaseHref: https://github.com/jline/jline3/releases
learnMoreText: JLine GitHub
learnMoreHref: https://github.com/jline/jline3
image: images/ui-jline.png
tags:
    - Terminal UI
dateAdded: 2026-02-09
---

JLine is Java's alternative to GNU Readline, providing advanced console input handling with portability, flexibility, and deep Java integration. The library offers line editing with Emacs and Vi modes, persistent command history with search, customizable tab completion for commands and file paths, syntax highlighting with custom rules, and password masking. JLine 4.0 brings full JPMS support (proper module-info.java), FFM terminal provider for JDK 22+ with Foreign Function & Memory API, and requires Java 11+ and Maven 4.0+. The modular structure includes terminal abstraction, line reading with completion, a command framework (jline-console), styling support, and built-in commands. Used by major tools like Maven, Gradle, Groovy, and Kotlin REPL, JLine is the standard choice for building interactive shells, REPLs, and sophisticated CLI applications in Java.

## Code Example

```java
import org.jline.reader.*;
import org.jline.terminal.*;

public class HelloJLine {
    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().build();
        LineReader reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build();
        
        String line = reader.readLine("Enter your name: ");
        terminal.writer().println("Hello, " + line + "!");
        terminal.flush();
    }
}
```

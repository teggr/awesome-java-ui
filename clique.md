---
name: Clique
status: Production-ready, Very Active
javaVersion: 21+
learningCurve: Easy
lastRelease: v4.0.3
lastReleaseHref: https://github.com/kusoroadeolu/Clique/releases
learnMoreText: Clique GitHub
learnMoreHref: https://github.com/kusoroadeolu/Clique
image: images/ui-clique.png
tags:
    - Terminal UI
dateAdded: 2026-02-13
---

Clique is a dependency-free, lightweight, and extensible CLI library for beautifying Java terminal applications. It provides a simple, markup-based syntax for styling terminal output that's far more readable than raw ANSI escape codes. The library offers a comprehensive feature set including a markup parser with intuitive tag syntax like `[red, bold]Error:[/]`, built-in themes (Catppuccin, Dracula, Gruvbox, Nord, Tokyo Night) through an extensible theme system, ASCII tables with multiple rendering styles, single-cell boxes with text wrapping, hierarchical text indentation, a fluent StyleBuilder API, and progress bars with animations and customizable styles.

What sets Clique apart is its zero-dependency design—no bloated transitive dependencies in your project—and its focus on developer experience with readable markup instead of cryptic ANSI codes. The framework supports custom themes through a simple ServiceLoader-based plugin architecture, allowing you to create and distribute themes as separate libraries. Progress bars feature smooth easing animations with multiple styles (Blocks, Lines, Bold, Classic, Dots) and support dynamic styling based on progress ranges. Tables provide compile-time header safety through a builder pattern, preventing common runtime errors. Whether you're building CLI tools, monitoring dashboards, or interactive terminal applications, Clique makes it easy to create professional-looking, styled output with minimal code.

## Code Example

```java
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.box.BoxType;
import io.github.kusoroadeolu.clique.table.TableType;

public class HelloClique {
    public static void main(String[] args) {
        // Simple markup parsing
        Clique.parser().print("[green, bold]Success:[/] Operation completed!");
        
        // Create a styled table
        Clique.table(TableType.DEFAULT)
            .addHeaders("Name", "Age", "Status")
            .addRows("Alice", "25", "Active")
            .addRows("Bob", "30", "Inactive")
            .render();
        
        // Create a box with text wrapping
        Clique.box(BoxType.ROUNDED)
            .withDimensions(40, 5)
            .content("Clique makes terminal styling easy!")
            .render();
    }
}
```

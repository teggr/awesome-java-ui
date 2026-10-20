---
name: Swing + FlatLaf
status: Actively-maintained
javaVersion: 8+ (11+ recommended for HiDPI)
learningCurve: Easy
lastRelease: FlatLaf 3.7.2 (July 2026)
lastReleaseHref: https://github.com/JFormDesigner/FlatLaf/releases
learnMoreText: FlatLaf Official Website
learnMoreHref: https://www.formdev.com/flatlaf/
image: images/ui-flatlaf.png
tags:
    - Desktop UI
dateAdded: 2026-02-09
---

Swing may be a legacy framework, but it's far from dead. The IntelliJ plugin ecosystem relies heavily on Swing, and modern look-and-feel libraries like FlatLaf have completely revitalized its appearance. FlatLaf brings flat, modern design inspired by IntelliJ IDEA and Darcula themes, with built-in light and dark modes, HiDPI/Retina support, and cross-platform consistency. It's actively developed by FormDev Software and used in production by commercial applications like Burp Suite, JOSM (OpenStreetMap editor), and countless IntelliJ plugins. If you're building IntelliJ plugins or maintaining Swing applications, FlatLaf makes them look contemporary with minimal effort.

## Code Example

```java
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class HelloSwingFlatLaf {
    public static void main(String[] args) {
        FlatDarkLaf.setup(); // Modern dark theme
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlatLaf Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel("Hello, Swing with FlatLaf!", SwingConstants.CENTER));
            frame.setSize(300, 200);
            frame.setVisible(true);
        });
    }
}
```

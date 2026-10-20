---
name: java-gtk
status: Experimental, Early Development
javaVersion: 11+
learningCurve: Moderate-Steep
lastRelease: v0.6.1 (July 2025)
lastReleaseHref: https://github.com/bailuk/java-gtk/releases
learnMoreText: java-gtk GitHub
learnMoreHref: https://github.com/bailuk/java-gtk
image: images/ui-gtk.png
tags:
    - Desktop UI
dateAdded: 2026-02-13
---

java-gtk provides Java bindings for GTK+ (GIMP Toolkit), enabling Java developers to create native Linux desktop applications using the GTK widget toolkit. GTK is the foundation of the GNOME desktop environment and many Linux applications, offering truly native Linux UI with platform integration, theming support (GTK themes), and adherence to GNOME Human Interface Guidelines. Unlike Swing or JavaFX which provide their own rendering, java-gtk wraps native GTK widgets, resulting in applications that look and feel completely native to Linux systems. The project is experimental and in early development, aimed at bringing modern GTK (GTK 3/4) capabilities to Java. While Java has historically had limited options for truly native Linux UI (most frameworks provide cross-platform solutions that don't always integrate perfectly with Linux desktops), java-gtk addresses this gap. However, given its early stage, it's not yet suitable for production use. For production Linux desktop development in Java, JavaFX or Swing (with appropriate look-and-feel) remain more mature options, though java-gtk shows promise for future native Linux integration.

## Code Example

```java
// Note: API is illustrative, project is in early development
import org.gtk.*;

public class HelloGTK {
    public static void main(String[] args) {
        Gtk.init(args);
        
        Window window = new Window();
        window.setTitle("java-gtk Example");
        window.setDefaultSize(400, 200);
        
        Box box = new Box(Orientation.VERTICAL, 10);
        
        Label label = new Label("Hello, GTK!");
        Button button = new Button("Click me");
        
        button.onClicked(() -> {
            label.setText("Button clicked!");
        });
        
        box.append(label);
        box.append(button);
        
        window.setChild(box);
        window.show();
        
        Gtk.main();
    }
}

// WARNING: Project is experimental
// Not recommended for production use
```

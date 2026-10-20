---
name: Casciian
status: Active Development
javaVersion: 8+
learningCurve: Moderate
lastRelease: v1.6.0
lastReleaseHref: https://github.com/crramirez/casciian/releases
learnMoreText: Casciian GitHub
learnMoreHref: https://github.com/crramirez/casciian
image: images/ui-casciian.png
tags:
    - Terminal UI
dateAdded: 2026-02-09
---

Casciian is a sophisticated text-based windowing system inspired by Borland's Turbo Vision, designed for building modern terminal UIs with advanced visual features. It supports translucent windows, layered images, and desktop effects that create a polished, game-like aesthetic—all within a terminal environment. The library works seamlessly across xterm-compatible terminals and can even run inside its own terminal window with full mouse support. Casciian combines the nostalgia of classic text UIs with contemporary features like pulsing button text, window animations, and multiple terminal support, making it perfect for creating visually impressive terminal applications.

Built on a foundation originally dedicated to the public domain and now maintained under the Apache License, Casciian provides a complete toolkit for developing complex text-based applications. The framework includes windows, buttons, labels, menus, dialogs, and layout managers, all working together to create sophisticated interfaces. Whether you're building system administration tools, monitoring applications, or interactive terminal-based experiences, Casciian offers the visual polish and feature set to stand out from traditional terminal UIs.

## Code Example

```java
import casciian.TApplication;
import casciian.TWindow;
import casciian.TButton;
import casciian.TLabel;

public class HelloCasciian {
    public static void main(String[] args) throws Exception {
        new TApplication() {
            {
                TWindow window = addWindow("Casciian Example", 2, 2, 40, 10);
                window.addLabel("Hello, Casciian!", 2, 2);
                window.addButton("&Close", 2, 4, () -> {
                    getApplication().exit();
                });
            }
        }.run();
    }
}
```

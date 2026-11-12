---
name: ModernUI
status: Active, Experimental
javaVersion: 17+
learningCurve: Moderate-Steep
lastRelease: v3.13.0 (June 2026)
lastReleaseHref: https://github.com/BloCamLimb/ModernUI/releases
learnMoreText: ModernUI GitHub
learnMoreHref: https://github.com/BloCamLimb/ModernUI
image: images/ui-modernui.png
tags:
    - Desktop UI
    - UI Components
dateAdded: 2026-11-12
---

ModernUI is a cross-platform Java UI runtime for desktop environments, centered on a retained view tree, layout, rich-text shaping, and GPU-accelerated 2D and 3D rendering. It uses LWJGL to access OpenGL, OpenGL ES, or Vulkan and aims for deterministic, pixel-identical rendering across Windows, Linux, and macOS rather than delegating drawing to native widget toolkits. Its Java-based components include a UI tree and layout system, text and rich-text support built around HarfBuzz, and reusable widgets. Java 17+ and modern GPU APIs are required. The core rendering and UI subsystems are in place, but standalone window backends and multi-window support are still in development, so ModernUI is not yet a turnkey Swing or JavaFX replacement. It is also used in a Minecraft integration that shares the host process, window, GPU context, and dependencies. ModernUI suits advanced custom-engine and embedded UI projects where rendering control and consistency outweigh a ready-made desktop application stack.

## Code Example

```java
import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;

public class GreetingView {
    public static View create(Context context) {
        var layout = new FrameLayout(context);
        var greeting = new TextView(context);
        greeting.setText("Hello, ModernUI!");
        layout.addView(greeting);
        return layout;
    }
}
```

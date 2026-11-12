---
name: DashboardFX
status: Active
javaVersion: 23+
learningCurve: Moderate
lastRelease: v0.9.0
lastReleaseHref: https://github.com/gleidsonmt/DashboardFx/tags
learnMoreText: DashboardFX GitHub
learnMoreHref: https://github.com/gleidsonmt/DashboardFx
image: images/ui-dashboardfx.png
tags:
    - Desktop UI
    - UI Components
dateAdded: 2026-11-12
---

DashboardFX is an open-source JavaFX desktop dashboard and component showcase, rather than a general-purpose UI framework. It demonstrates how Java and CSS can be combined to build a polished dashboard with navigable views, charts, cards, and reusable UI elements. The project includes in-app tutorials that explain how to build and style components, and is built with Gradle and JavaFX 23 alongside related libraries such as the Glad toolkit and Presentation module. Its responsive layout and themeable CSS make it useful as a learning resource or starting point for Java desktop dashboard projects. DashboardFX is a complete sample application with its own dependencies and architecture, so adopting individual widgets may require adapting source code. It is licensed under GPL-3.0.

## Code Example

```java
import io.github.gleidsonmt.dashboardfx.Main;
import io.github.gleidsonmt.glad.base.Root;
import io.github.gleidsonmt.glad.theme.Css;
import io.github.gleidsonmt.glad.theme.Font;
import io.github.gleidsonmt.glad.theme.ThemeProvider;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Root(new Main()), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Blue Galaxy");
        ThemeProvider.install(scene, Css.ALL, Font.INSTAGRAM, Font.POPPINS);
        stage.show();
    }
}
```

---
name: Gluon
status: Production-ready, Commercial & Open Source
javaVersion: 17+ (21+ recommended)
learningCurve: Moderate
lastRelease: Scene Builder 26.0.0 (April 2026)
lastReleaseHref: https://github.com/gluonhq/scenebuilder/releases
learnMoreText: Gluon Official Website
learnMoreHref: https://gluonhq.com/
image: images/ui-gluonhq.png
tags:
    - Desktop UI
    - Mobile
    - Build Tools
dateAdded: 2026-02-25
---

Gluon is a Java-focused company and platform that co-leads the OpenJFX project and provides a suite of commercial and open-source tools for building cross-platform JavaFX applications. Their flagship open-source product, Scene Builder, is a drag-and-drop visual designer for FXML-based JavaFX UIs, enabling rapid interface design with clear separation between design and application logic. GluonFX extends JavaFX with additional components including a Rich Text Area, interactive Maps, and Emoji support. For mobile development, Gluon Mobile leverages GraalVM native image to compile JavaFX apps for iOS and Android from a single codebase. Gluon also provides JavaFX Long Term Support (LTS) contracts, commercial support for enterprises running JavaFX 11+, IDE plugins for IntelliJ IDEA, Eclipse, and NetBeans, and the GluonFX Maven/Gradle plugins that streamline the build and deployment pipeline. Gluon's deep involvement in the JavaFX ecosystem—from tooling to core contributions—makes it the go-to commercial partner for JavaFX-based enterprise, desktop, and mobile development.

## Code Example

```java
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class HelloGluon extends MobileApplication {

    @Override
    public void init() {
        addViewFactory(HOME_VIEW, () -> {
            Label label = new Label("Hello, Gluon Mobile!");
            View view = new View(new StackPane(label));
            view.showingProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    AppBar appBar = getAppBar();
                    appBar.setTitleText("Gluon Mobile");
                }
            });
            return view;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

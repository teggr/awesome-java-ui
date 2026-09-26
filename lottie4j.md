---
name: Lottie4J
status: Active
javaVersion: 21+
learningCurve: Moderate
lastRelease: v1.2.5 (July 2026)
learnMoreText: Lottie4J Website
learnMoreHref: https://lottie4j.com/
image: images/ui-lottie4j.png
tags:
    - Desktop UI
    - UI Components
---

Lottie4J is a Java library for working with Lottie animations natively, with a strong focus on JavaFX desktop applications. It parses Lottie JSON files into typed Java objects and can also write valid Lottie files back to disk, making it useful for both playback and tooling workflows. Its `fxplayer` module renders animations directly on a JavaFX `Canvas`, so teams can avoid embedding a browser engine or JavaScript bridge just to show motion assets.

The project is open source, available on Maven Central, and under active development with regular release notes and progress updates. Lottie4J is a strong fit for Java 21+ applications that need lightweight, offline-friendly animation support with small runtime footprints. It is especially practical for dashboards, media-rich desktop tools, launchers, and any JavaFX UI that benefits from modern vector animation assets.

## Code Example

```java
import com.lottie4j.core.loader.LottieFileLoader;
import com.lottie4j.core.model.Animation;
import com.lottie4j.fxplayer.LottiePlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Lottie4JDemo extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Animation animation = LottieFileLoader.load(new File("animation.json"));
        Scene scene = new Scene(
            new LottiePlayer(animation),
            animation.width() != null ? animation.width() : 500,
            animation.height() != null ? animation.height() : 500
        );
        stage.setTitle("Lottie4J Demo");
        stage.setScene(scene);
        stage.show();
    }
}
```

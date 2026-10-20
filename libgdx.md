---
name: LibGDX
status: Production-ready, Active
javaVersion: 8-21 (varies by platform, see notes)
learningCurve: Moderate
lastRelease: v1.14.2 (June 2026)
lastReleaseHref: https://github.com/libgdx/libgdx/releases
learnMoreText: LibGDX Official Website
learnMoreHref: https://libgdx.com/
image: images/ui-libgdx.png
tags:
    - Game Development
    - Desktop UI
    - Mobile
    - Web UI
dateAdded: 2026-02-13
---

LibGDX is a mature, battle-tested cross-platform game development framework based on OpenGL (ES) that works on Windows, Linux, macOS, Android, iOS, and HTML5—all with the same code base. **Note on Java versions:** Desktop supports Java 8-21+ (LTS versions recommended). iOS via RoboVM supports Java 8 with language feature limitations. Android support varies by Android version (7-14 supports different Java levels). HTML5 via GWT has Java language limitations. See [LibGDX Java version guide](https://libgdx.com/wiki/articles/java-development-kit-selection) for details. It provides a complete game development environment with 2D and 3D rendering, physics, audio, input handling, and an extensive set of tools. LibGDX's Scene2D UI system offers a complete widget library for creating game interfaces with layouts, buttons, tables, dialogs, and more. The framework emphasizes performance with careful garbage collection avoidance through custom collections and API design optimized for ART/JavaScript runtimes. LibGDX is open source (Apache 2.0), well-documented, and backed by an extremely active community that runs regular game jams. It's used by countless commercial games including titles on Steam, and its "batteries included" philosophy means you can focus on game logic rather than low-level OpenGL details. Perfect for indie developers and studios wanting professional, cross-platform game development with a proven track record.

## Code Example

```java
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class HelloLibGDX extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;

    @Override
    public void create() {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        Label label = new Label("Hello, LibGDX!", skin);
        TextButton button = new TextButton("Click me", skin);
        button.addListener(event -> {
            label.setText("Button clicked!");
            return true;
        });
        
        table.add(label).padBottom(20);
        table.row();
        table.add(button);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }
}
```

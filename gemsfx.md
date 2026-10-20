---
name: GemsFX
status: Production-ready, Very Active
javaVersion: 21+
learningCurve: Easy-Moderate
lastRelease: v4.5.0 (September 2026)
lastReleaseHref: https://github.com/dlsc-software-consulting-gmbh/GemsFX/releases
learnMoreText: GemsFX on GitHub
learnMoreHref: https://github.com/dlsc-software-consulting-gmbh/GemsFX
image: images/ui-gemsfx.png
tags:
    - Desktop UI
    - UI Components
dateAdded: 2026-02-27
---

GemsFX is a rich collection of custom JavaFX controls and utilities developed by DLSC Software & Consulting GmbH and released under the Apache 2.0 licence. It extends the standard JavaFX component set with polished, production-grade widgets that would otherwise require significant custom development effort. The library covers a wide range of UI needs: date and time controls (CalendarView, CalendarPicker, DateRangePicker, TimePicker, DurationPicker), advanced text input (SearchField, TagsField, EmailField, EnhancedPasswordField, ExpandingTextArea), layout helpers (ResponsivePane, DrawerStackPane, PowerPane), media controls (PhotoView, AvatarView, SVGImageView), and utility components like FilterView, InfoCenterPane, and SessionManager.

GemsFX targets JavaFX 23+ and Java 21+, aligns with the modern JavaFX ecosystem, and integrates the optional AtlantaFX stylesheet for contemporary styling. Available from Maven Central with a single dependency, it is easy to add to existing JavaFX projects. The library is actively maintained with frequent releases and is showcased on JFX Central. It is the go-to choice for JavaFX developers who need feature-rich, well-styled UI components without building them from scratch.

## Code Example

```java
import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.SearchField;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.List;
import java.util.stream.Collectors;

public class GemsFXDemo extends Application {

    @Override
    public void start(Stage stage) {
        // SearchField with suggestion model
        SearchField<String> searchField = new SearchField<>();
        searchField.setSuggestionProvider(request ->
            List.of("Apple", "Banana", "Cherry", "Date", "Elderberry")
                .stream()
                .filter(s -> s.toLowerCase().contains(request.getUserText().toLowerCase()))
                .collect(Collectors.toList())
        );
        searchField.setConverter(new StringConverter<>() {
            public String toString(String s) { return s != null ? s : ""; }
            public String fromString(String s) { return s; }
        });

        // CalendarPicker
        CalendarPicker calendarPicker = new CalendarPicker();

        VBox root = new VBox(10,
            new Label("Search:"), searchField,
            new Label("Pick a date:"), calendarPicker
        );
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("GemsFX Demo");
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
```

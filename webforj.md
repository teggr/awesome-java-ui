---
name: webforJ
status: Production-ready, Enterprise-focused
javaVersion: 17+ (21+ recommended) 
learningCurve: Easy
lastRelease: v26.02 (September 2026)
lastReleaseHref: https://github.com/webforj/webforj/releases
learnMoreText: webforJ Documentation
learnMoreHref: https://docs.webforj.com/
image: images/ui-webforj.png
tags:
    - Web UI
    - UI Components
dateAdded: 2026-02-17
---

webforJ is a Java web framework built on composition — views assemble from server-side components that the DWC (Dynamic Web Client), webforJ's rendering engine and design system, renders as matching web components in the browser, synchronizing all state transparently. The component set spans data tables, application shells, form fields, dialogs, and more, all sharing a cohesive visual language with a companion Figma kit. The framework goes well beyond rendering, addressing routing, data management, security, and internationalization as first-class concerns. Any web component from the JavaScript ecosystem can be brought into type-safe Java, and a Webswing connector lets legacy Swing desktop applications run in the browser for incremental modernization. Deep Spring Boot integration makes it a natural choice for teams already working in the Spring ecosystem.

## Code Example

```java
import com.webforj.component.Composite;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.field.TextField;
import com.webforj.component.icons.FeatherIcon;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.toast.Toast;
import com.webforj.router.annotation.Route;

@Route("/")
public class HelloWorldView extends Composite<FlexLayout> {

  private FlexLayout self = getBoundComponent();
  private TextField hello = new TextField("What is your name?");
  private Button btn = new Button("Say Hello");

  public HelloWorldView(){
    self.setDirection(FlexDirection.COLUMN);
    self.setMaxWidth(300);
    self.setStyle("margin", "1em auto");

    btn.setPrefixComponent(FeatherIcon.BELL.create())
        .setTheme(ButtonTheme.PRIMARY)
        .onClick(e -> {
          Toast.show("Welcome to webforJ Starter " + hello.getValue() + "!", Theme.GRAY);
        });

    self.add(hello, btn);
  }
}
```

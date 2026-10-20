---
name: Domino UI
status: Active, Open Source
javaVersion: 11+
learningCurve: Moderate
lastRelease: v2.1.3
lastReleaseHref: https://github.com/DominoKit/domino-ui/releases
learnMoreText: Domino UI Website
learnMoreHref: https://dominokit.com/home
image: images/ui-domino-ui.png
tags:
    - Web UI
    - UI Components
dateAdded: 2026-02-13
---

Domino UI is a Material Design component library for GWT (Google Web Toolkit) and J2CL (Java-to-JavaScript compiler), providing a comprehensive set of modern, responsive UI widgets. Built on top of Elemento (a lightweight library for working with DOM), Domino UI offers over 50 components including cards, dialogs, forms, data tables, navigation elements, and more—all following Material Design guidelines. Unlike older GWT component libraries, Domino UI embraces modern web standards and provides a fluent, type-safe API for building web UIs entirely in Java. The library includes built-in theming support, responsive layouts, animations, and accessibility features. Domino UI is particularly relevant as part of the broader J2CL ecosystem, which aims to modernize GWT by compiling Java directly to modern JavaScript (rather than ES5). For teams committed to writing web applications in Java without JavaScript, Domino UI provides a contemporary component library that doesn't feel dated, offering Material Design aesthetics with the productivity benefits of Java's type system and tooling.

## Code Example

```java
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.cards.Card;
import org.dominokit.domino.ui.notifications.Notification;
import elemental2.dom.DomGlobal;

public class HelloDominoUI {
    public void onModuleLoad() {
        Card card = Card.create("Welcome")
            .appendChild(Button.create("CLICK ME")
                .addClickListener(evt -> 
                    Notification.create("Hello, Domino UI!")
                        .info()
                        .show()
                )
            );
        
        DomGlobal.document.body.appendChild(card.element());
    }
}

// Compiles to modern JavaScript with J2CL
// Material Design components out of the box
// Type-safe, fluent API
```

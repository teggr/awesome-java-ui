---
name: Shadleaf
status: Active
javaVersion: 17+ (Spring Boot 4)
learningCurve: Easy
lastRelease: 1.0.3
lastReleaseHref: https://github.com/wimdeblauwe/shadleaf/releases
learnMoreText: Shadleaf Website
learnMoreHref: https://shadleaf.dev
image: images/ui-shadleaf.png
tags:
    - Web UI
    - UI Components
dateAdded: 2026-11-16
---

Shadleaf brings shadcn/ui-style components to Spring Boot and Thymeleaf. Add the starter, write component tags in your templates, and the server renders them.

- Tags like `<sl:card>`, `<sl:dialog>` and `<sl:button>`, with no JavaScript framework needed
- The jar ships compiled CSS and JavaScript, so a plain Maven build is enough; Tailwind users can switch to an embedded bundle
- Theming through CSS variables (`--primary`, `--radius`, `--ring`) in light and dark mode
- Nine predefined skins, switched with the `shadleaf.skin` property
- Dialogs, menus and toasts keep working when htmx swaps them in, and an `HX-Trigger` header can show a toast
- Invalid prop values fail on first render with a message listing the valid options
- Generated web-types give IntelliJ IDEA completion for components and props
- Ten copyable application layouts, plus Markdown docs for coding agents
- Licensed under Apache-2.0

## Code Example

```html
<!-- pom.xml: io.github.wimdeblauwe:shadleaf-spring-boot-starter:1.0.3 -->
<html xmlns:th="http://www.thymeleaf.org" xmlns:sl="https://shadleaf.dev/sl">
<head>
  <th:block th:replace="~{sl/layout :: theme-script}"></th:block>
  <th:block th:replace="~{sl/layout :: assets}"></th:block>
</head>
<body>
  <sl:card>
    <sl:card-header>
      <sl:card-title level="3">Order #1042</sl:card-title>
      <sl:card-description>Placed on 12 September</sl:card-description>
      <sl:card-action><sl:badge variant="secondary">Shipped</sl:badge></sl:card-action>
    </sl:card-header>
    <sl:card-content>
      <p>Three items, delivered to the Antwerp warehouse.</p>
    </sl:card-content>
    <sl:card-footer>
      <sl:button as="a" href="/orders/1042" variant="outline">View order</sl:button>
    </sl:card-footer>
  </sl:card>
</body>
</html>
```

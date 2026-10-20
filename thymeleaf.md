---
name: Thymeleaf
status: Production-ready
javaVersion: 8+
learningCurve: Easy
lastRelease: 3.1.5.RELEASE (April 2026)
lastReleaseHref: https://github.com/thymeleaf/thymeleaf/releases
learnMoreText: Thymeleaf Official Website
learnMoreHref: https://www.thymeleaf.org/
image: images/ui-thymeleaf.png
tags:
    - Web UI
dateAdded: 2026-02-09
---

Thymeleaf is a modern server-side template engine that emphasizes natural templating—templates are valid HTML that can be viewed directly in browsers without running the application. This makes collaboration with designers easier since templates aren't filled with non-HTML syntax. Thymeleaf integrates deeply with the Spring ecosystem, particularly Spring Boot, where it's often the default templating choice. The framework supports expression language for accessing model data, fragment inclusion for reusable components, and natural formatting for dates, numbers, and text. For Spring Boot applications needing traditional server-side rendered views with good designer-developer collaboration, Thymeleaf is the standard choice.

## Code Example

```java
@Controller
public class HelloController {
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello, Thymeleaf!");
        return "hello";
    }
}

// Thymeleaf template (hello.html):
// <!DOCTYPE html>
// <html xmlns:th="http://www.thymeleaf.org">
// <body>
//   <h1 th:text="${message}">Placeholder</h1>
// </body>
// </html>
```

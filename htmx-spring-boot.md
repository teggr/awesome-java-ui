---
name: HTMX + Spring Boot
status: Modern, Very Active
javaVersion: 11+ (Spring Boot 2.x+), 17+ (Spring Boot 3.x+)
learningCurve: Easy
lastRelease: v6.0.1
lastReleaseHref: https://github.com/wimdeblauwe/htmx-spring-boot/releases
learnMoreText: htmx-spring-boot GitHub
learnMoreHref: https://github.com/wimdeblauwe/htmx-spring-boot
image: images/ui-htmx.png
tags:
    - Web UI
dateAdded: 2026-02-09
---

HTMX brings hypermedia-driven development to modern web apps by adding AJAX, WebSockets, and Server-Sent Events via HTML attributes—no JavaScript needed. The htmx-spring-boot library from Wim Deblauwe (with contributions from Spring team members like Oliver Drotbohm) provides annotations like `@HxRequest`, `@HxTrigger`, and `@HxRefresh`, along with specialized views for redirects and out-of-band swaps. The Spring ViewComponent library by Thomas Schhly takes this further with component-based architecture similar to React/Vue but entirely server-side, where ViewComponents are Spring-managed beans with dependency injection. This approach combines the simplicity of server-side rendering with the interactivity of modern SPAs, using template engines like Thymeleaf or JTE. It's gaining strong traction in the Spring community for teams who want dynamic interfaces without heavy JavaScript frameworks.

## Code Example

```java
@Controller
public class HelloController {
    
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello, HTMX + Spring!");
        return "hello";
    }
    
    @GetMapping("/update")
    @HxRequest
    public String update(Model model) {
        model.addAttribute("message", "Updated via HTMX!");
        return "hello :: message";
    }
}

// Thymeleaf template (hello.html):
// <div id="content">
//   <p th:fragment="message" th:text="${message}"></p>
//   <button hx-get="/update" hx-target="#content" hx-swap="innerHTML">
//     Update
//   </button>
// </div>
```

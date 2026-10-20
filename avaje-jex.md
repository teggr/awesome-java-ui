---
name: Avaje Jex
status: Production-ready, Active
javaVersion: 11+
learningCurve: Easy
lastRelease: v3.6
lastReleaseHref: https://github.com/avaje/avaje-jex/releases
learnMoreText: Avaje Jex GitHub
learnMoreHref: https://github.com/avaje/avaje-jex
image: images/ui-avaje-jex.png
tags:
    - Web Framework
    - Microservices
dateAdded: 2026-02-13
---

Avaje Jex is a micro web framework for Java focused on simplicity and performance. Part of the Avaje ecosystem alongside Avaje HTTP, Avaje JSON, and other lightweight libraries, Jex provides a minimal, unopinionated foundation for building REST APIs and web services. The framework offers multiple backend implementations (Jetty, Netty, Undertow) allowing you to choose the web server that best fits your needs. Avaje Jex emphasizes type-safe routing with annotation-based or programmatic API definitions, and integrates seamlessly with other Avaje libraries for dependency injection, JSON serialization, validation, and more. Unlike heavyweight frameworks, Jex has minimal abstractions and stays close to the underlying HTTP semantics, making it easy to understand and debug. The framework supports OpenAPI generation, WebSockets, static file serving, and middleware/interceptors for cross-cutting concerns. It's ideal for microservices, serverless functions, and lightweight APIs where you want control over dependencies and minimal framework overhead.

## Code Example

```java
import io.avaje.jex.Jex;

public class HelloAvaje {
    public static void main(String[] args) {
        
        Jex app = Jex.create()
            .port(8080)
            .routing(routing -> routing
                .get("/", ctx -> ctx.text("Hello, Avaje Jex!"))
                
                .get("/hello/{name}", ctx -> {
                    String name = ctx.pathParam("name");
                    ctx.json(Map.of(
                        "message", "Hello, " + name + "!",
                        "timestamp", System.currentTimeMillis()
                    ));
                })
                
                .post("/echo", ctx -> {
                    String body = ctx.body();
                    ctx.text("You sent: " + body);
                })
            )
            .start();
        
        System.out.println("Server started at http://localhost:8080");
    }
}
```

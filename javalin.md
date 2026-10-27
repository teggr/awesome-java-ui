---
name: Javalin
status: Production-ready, Active
javaVersion: 17+
learningCurve: Easy
lastRelease: javalin-parent-7.2.4
lastReleaseHref: https://github.com/javalin/javalin/releases
learnMoreText: Javalin Official Website
learnMoreHref: https://javalin.io/
image: images/ui-javalin.png
tags:
    - Web Framework
    - Microservices
dateAdded: 2026-02-13
---

Javalin is a simple, lightweight web framework for Java and Kotlin built on top of Jetty 12. Unlike other frameworks, Javalin has very few concepts to learn—you never extend classes and rarely implement interfaces, making it one of the easiest Java web frameworks to pick up. Javalin 7 requires Java 17 and brings an improved configuration model, a more consistent plugin API, and a cleaner overall architecture. It's designed with inter-operability in mind, so applications are built identically in both Java and Kotlin. Javalin embraces a simple, blocking programming model that's easy to reason about, but automatically switches to asynchronous mode when you set a Future as result. It includes built-in OpenAPI support with Swagger UI and ReDoc, comprehensive WebSocket and Server-Sent Events support, and access to all of Jetty's configuration options including SSL and HTTP3. With over 2 million monthly downloads and 8.2k GitHub stars, Javalin is perfect for microservices, REST APIs, and lightweight web applications where you want minimal ceremony and maximum clarity.

## Code Example

```java
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class HelloJavalin {
    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.jetty.port = 7070;
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.routes.apiBuilder(() -> {
                path("users", () -> {
                    get(UserController::getAll);
                    post(UserController::create);
                    path("{user-id}", () -> {
                        get(UserController::getOne);
                        patch(UserController::update);
                        delete(UserController::delete);
                    });
                });
                ws("/events", ws -> {
                    ws.onMessage(ctx -> ctx.send(ctx.message()));
                });
            });
        }).start();
    }
}
```

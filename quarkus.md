---
name: Quarkus
status: Production-ready, Active
javaVersion: 11+ (17+ recommended)
learningCurve: Moderate
lastRelease: v3.40.1.1 (October 2026)
lastReleaseHref: https://github.com/quarkusio/quarkus/releases
learnMoreText: Quarkus Official Website
learnMoreHref: https://quarkus.io/
image: images/ui-quarkus.png
tags:
    - Web Framework
    - Cloud-Native
dateAdded: 2026-02-13
---

Quarkus is a Kubernetes-native Java framework tailored for GraalVM and OpenJDK HotSpot, designed for building modern cloud-native and serverless applications. Now part of the Commonhaus Foundation, it provides amazingly fast boot times and incredibly low memory footprint by performing optimization at build-time through a technique called "compile time boot." Quarkus offers live reload development with near-instant feedback, reactive and imperative programming models on the same reactive core, and deep integration with over 50 best-of-breed libraries including RESTEasy, Hibernate, Vert.x, and more. It excels at container-first deployments with sub-second startup times and minimal RSS memory usage, making it ideal for Kubernetes environments, microservices architectures, and serverless functions. The framework emphasizes developer joy through unified configuration, zero-config defaults, and streamlined code for common use cases while remaining flexible for advanced scenarios.

## Code Example

```java
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class HelloResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, Quarkus!";
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

// Run with: ./mvnw quarkus:dev
// Native build: ./mvnw package -Pnative
```

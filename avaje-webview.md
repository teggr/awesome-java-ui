---
name: Avaje WebView
status: Experimental, Early Development
javaVersion: 11+
learningCurve: Easy
lastRelease: v0.15
lastReleaseHref: https://github.com/avaje/avaje-webview/releases
learnMoreText: Avaje WebView GitHub
learnMoreHref: https://github.com/avaje/avaje-webview
image: images/ui-avaje-webview.png
tags:
    - Desktop UI
    - Web UI
dateAdded: 2026-02-13
---

Avaje WebView provides Java bindings for native webview components, enabling developers to create cross-platform desktop applications using web technologies (HTML, CSS, JavaScript) with a Java backend. Similar in concept to Electron but more lightweight, Avaje WebView embeds platform-native webview controls (WebKit on macOS/Linux, WebView2 on Windows) rather than bundling an entire Chromium instance. This results in significantly smaller application sizes and lower memory footprints compared to Electron-based alternatives. The library allows bidirectional communication between Java code and JavaScript in the webview, making it possible to build hybrid desktop applications where the UI is HTML-based but business logic and system integration happens in Java. While still in early development, Avaje WebView shows promise for Java developers wanting to build modern desktop UIs using web technologies without the overhead of full browser engines. It's suitable for internal tools, utilities, and applications where native webview capabilities are sufficient and bundle size matters.

## Code Example

```java
import io.avaje.webview.WebView;
import io.avaje.webview.WebViewBuilder;

public class HelloAvaje WebView {
    public static void main(String[] args) {
        WebView webView = WebViewBuilder.create()
            .title("Avaje WebView Example")
            .size(800, 600)
            .content("""
                <html>
                  <body>
                    <h1>Hello, Avaje WebView!</h1>
                    <button onclick="callJava()">Call Java</button>
                    <script>
                      function callJava() {
                        javaInterface.showMessage("Hello from JS!");
                      }
                    </script>
                  </body>
                </html>
            """)
            .bind("javaInterface", new JavaInterface())
            .build();
        
        webView.show();
    }
    
    public static class JavaInterface {
        public void showMessage(String msg) {
            System.out.println("Message from JS: " + msg);
        }
    }
}
```

---
name: WasmCoffee
status: Active
javaVersion: N/A (Java-inspired language; browser-based)
learningCurve: Easy-Moderate
lastRelease: v0.0.15
learnMoreText: Official Website
learnMoreHref: https://wasmcoffee.com/
image: images/ui-wasmcoffee.png
tags:
    - Web UI
    - Compiler/Transpiler
dateAdded: 2026-10-27
---

WasmCoffee is a browser-based playground for a Java-inspired language that compiles to WebAssembly GC (Wasm-GC) and runs programs in the same tab. It is not a Java compiler or runtime: programs use a Java-like language subset and require a modern browser with Wasm-GC support, but no JDK or JVM. The compiler is an Odin-built WebAssembly module that runs in a Web Worker, keeping compilation client-side.

The interface combines a multi-file editor and console with HTML5 Canvas 2D output, keyboard and pointer callbacks, and bundled graphics and game-development APIs. Supported syntax includes classes, interfaces, generics, records, and pattern matching; compiled `.wasm` binaries can also be exported. WasmCoffee is useful for language experiments, teaching, browser-based demos, and small interactive games without a local setup. It is freeware rather than an open-source Java framework, so review its EULA and browser requirements before adopting it.

## Code Example

```java
public class HelloConsole {
    public static void main(String[] args) {
        System.out.println("Hello from WasmCoffee!");
    }
}
```

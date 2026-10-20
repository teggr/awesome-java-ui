---
name: NetBeans Platform
status: Production-ready, Mature
javaVersion: 8+
learningCurve: Steep
lastRelease: Apache NetBeans 31 (August 2026)
lastReleaseHref: https://github.com/apache/netbeans/releases
learnMoreText: NetBeans Platform Official Website
learnMoreHref: https://netbeans.apache.org/
image: images/ui-netbeans.png
tags:
    - Desktop UI
dateAdded: 2026-02-09
---

The NetBeans Platform is a full Rich Client Platform (RCP) for building modular desktop applications. NetBeans IDE itself is built on this platform, demonstrating its scalability for large, complex applications. The platform provides 100+ modules covering window management, auto-update systems, file system abstractions, wizards, property sheets, and more. It's Swing-based and includes the Matisse visual GUI builder for drag-and-drop interface design. If you're building complex, modular desktop applications that need plugin architectures and extensive built-in services, NetBeans Platform provides a comprehensive foundation.

## Code Example

```java
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;
import javax.swing.JLabel;
import java.awt.BorderLayout;

public class HelloNetBeans extends ModuleInstall {
    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            TopComponent tc = new TopComponent();
            tc.setLayout(new BorderLayout());
            tc.add(new JLabel("Hello, NetBeans Platform!"), BorderLayout.CENTER);
            tc.setDisplayName("Hello Example");
            tc.open();
            tc.requestActive();
        });
    }
}
```

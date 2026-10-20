---
name: CezveRender
status: Experimental
javaVersion: 17+
learningCurve: Steep
lastRelease: v1.1
lastReleaseHref: https://github.com/BoraYalcinn/CezveRender/tags
learnMoreText: CezveRender GitHub
learnMoreHref: https://github.com/BoraYalcinn/CezveRender
image: images/ui-cezverender.png
tags:
    - Game Development
    - Desktop UI
dateAdded: 2026-03-12
---

CezveRender is a real-time 3D renderer built from scratch in Java using OpenGL 3.3 and LWJGL. Created as a learning-focused personal project, it implements core graphics programming concepts including shadow mapping, physically-based lighting models, and scene management entirely in Java. The renderer supports directional, point, and spot lights — each with their own shadow mapping techniques (orthographic, cubemap, and perspective respectively) — along with Phong shading per material. OBJ model loading is handled via Assimp (through LWJGL bindings), and the project includes a live ImGui-powered editor for adding, removing, and transforming scene objects and lights at runtime. Additional features include a procedural mesh generator, skybox rendering, and an adjustable shadow bias debugger. The tech stack is built on LWJGL for low-level OpenGL/GLFW bindings, JOML for vector and matrix math, imgui-java for the in-scene editor UI, and STB for image loading. While experimental and not production-ready, CezveRender is an excellent reference for Java developers interested in learning how to implement a rendering pipeline from scratch on the JVM.

## Code Example

```java
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL33.*;

// Basic shader program setup in CezveRender style
public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexSrc, String fragmentSrc) {
        int vertShader = compileShader(GL_VERTEX_SHADER, vertexSrc);
        int fragShader = compileShader(GL_FRAGMENT_SHADER, fragmentSrc);

        programId = glCreateProgram();
        glAttachShader(programId, vertShader);
        glAttachShader(programId, fragShader);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program link error: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertShader);
        glDeleteShader(fragShader);
    }

    private int compileShader(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }

    public void setUniform(String name, float value) {
        glUniform1f(glGetUniformLocation(programId, name), value);
    }

    public void cleanup() { glDeleteProgram(programId); }
}
```

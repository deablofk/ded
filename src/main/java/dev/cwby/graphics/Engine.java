package dev.cwby.graphics;

import dev.cwby.graphics.layout.RegionNode;
import dev.cwby.input.IKeyHandler;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine {

    public long window;
    private int width = 1280;
    private int height = 720;
    private GLFWFramebufferSizeCallback resizeCallback;
    private IKeyHandler keyHandler;
    private IRender renderer;

    public void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if (keyHandler != null) {
            glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> keyHandler.handleKey(key, action, mods));
            glfwSetCharCallback(window, (windowHandle, codepoint) -> keyHandler.handleChar(codepoint));
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(GLFW_TRUE);
        GL.createCapabilities();
    }

    public void destroyGLFW() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
            errorCallback.close();
        }
    }

    public void loop() {
        if (renderer == null) {
            throw new RuntimeException("Renderer cant be null");
        }
        final double targetFPS = 100.0;
        final double targetFrameTime = 1.0 / targetFPS;
        while (!glfwWindowShouldClose(window)) {
            double frameStartTime = glfwGetTime();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderer.render(width, height);

            glfwSwapBuffers(window);
            glfwWaitEventsTimeout(targetFrameTime);
            double frameEndTime = glfwGetTime();
            double elapsedTime = frameEndTime - frameStartTime;

            if (elapsedTime < targetFrameTime) {
                try {
                    Thread.sleep((long) ((targetFrameTime - elapsedTime) * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setKeyHandler(IKeyHandler keyHandler) {
        this.keyHandler = keyHandler;
    }

    public void setRenderer(IRender renderer) {
        this.renderer = renderer;
        if (renderer != null) {
            if (resizeCallback != null) {
                resizeCallback.free();
                resizeCallback.close();
            }
            resizeCallback = glfwSetFramebufferSizeCallback(window, (_, width, height) -> {
                renderer.onResize(width, height);
                this.width = width;
                this.height = height;
            });

        }
    }
}

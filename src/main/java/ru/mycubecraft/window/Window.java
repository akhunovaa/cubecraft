package ru.mycubecraft.window;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static Window window = null;
    private final int width;
    private final int height;
    private final String title;
    private float red = 0.0f;
    private final float green = 0.0f;
    private final float blue = 0.0f;
    private final float alpha = 1.0f;
    private long glfwWindow;
    private KeyboardListener keyboardListener;
    private MouseListener mouseListener;

    private Window() {
        this.width = 800;
        this.height = 600;
        this.title = "CubeCraft Game";
    }

    public static Window getInstance() {
        if (window == null) {
            window = new Window();
        }
        return window;
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the memory after the loop exists
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and the free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }


    public void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);

        // Create the window where glfwWindow stores the memory address of created window
        this.glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        this.mouseListener = MouseListener.getInstance();
        glfwSetCursorPosCallback(this.glfwWindow, this.mouseListener::mousePositionCallback);
        glfwSetMouseButtonCallback(this.glfwWindow, this.mouseListener::mouseButtonCallback);
        glfwSetScrollCallback(this.glfwWindow, this.mouseListener::mouseScrollCallback);

        this.keyboardListener = KeyboardListener.getInstance();
        glfwSetKeyCallback(this.glfwWindow, this.keyboardListener::keyCallback);

        // Make the OpenGL context current
        glfwMakeContextCurrent(this.glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        GL.createCapabilities();
    }


    public void loop() {
        while (!glfwWindowShouldClose(this.glfwWindow)) {

            // Poll events
            glfwPollEvents();

            glClearColor(this.red, this.green, this.blue, this.alpha);
            glClear(GL_COLOR_BUFFER_BIT);

            if (this.keyboardListener.isKeyPressed(GLFW_KEY_EQUAL)) {
                this.red += 0.01f;
            } else if (this.keyboardListener.isKeyPressed(GLFW_KEY_MINUS)) {
                this.red -= 0.01f;
            }

            glfwSwapBuffers(this.glfwWindow);
        }
    }


}

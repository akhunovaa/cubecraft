package ru.mycubecraft.window;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private int width, height;
    private String title;
    private  long glfwWindow;

    private static Window window = null;

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

        // Make the OpenGL context current
        glfwMakeContextCurrent(this.glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        GL.createCapabilities();
    }


    public void loop() {
        while(!glfwWindowShouldClose(this.glfwWindow)){

            // Poll events
            glfwPollEvents();

            glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(this.glfwWindow);
        }
    }






}

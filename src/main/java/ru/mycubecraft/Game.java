package ru.mycubecraft;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Callback;
import ru.mycubecraft.scene.LevelScene;
import ru.mycubecraft.scene.Scene;
import ru.mycubecraft.window.Window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glFlush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {

    public static final boolean DEBUG = has("debug", true);
    private static final boolean SHOW_TRIANGLES = has("SHOW_TRIANGLES", false);
    private static final boolean CULL_FACE = has("CULL_FACE", true);
    private static final boolean VSYNC = has("VSYNC", false);
    public static GLCapabilities caps;
    private final Window gameWindow = Window.getInstance();
    private Scene currentScene;
    private Callback debugProc;

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "14");

        /*
        Headless mode is mainly useful in those systems that don't have a graphical display, typically the servers.
        Many applications use graphical displays to do things that are not necessarily needed to be seen, for instance drawing an image and then saving it to disk.
        if you run such a program on a server (ssh connections only, no graphic endpoint) you get an exception in normal mode, you get the program ran in headless mode.
         */
        System.setProperty("java.awt.headless", "true");

        new Game().run();
    }

    /**
     * Determines if the OpenGL context supports version 3.2.
     *
     * @return true, if OpenGL context supports version 3.2, else false
     */
    public static boolean isDefaultContext() {
        return GL.getCapabilities().OpenGL32;
    }

    public static boolean has(String prop, boolean def) {
        String value = System.getProperty(prop);
        return value != null ? value.isEmpty() || Boolean.parseBoolean(value) : def;
    }

    /**
     * Initialize and run the game.
     */
    private void run() throws InterruptedException {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        gameWindow.createWindow();
        gameWindow.registerWindowCallbacks();
        gameWindow.setWindowPosition();

        initGLResources();

        /* Run logic updates and rendering in a separate thread */
        Thread updateAndRenderThread = createAndStartUpdateAndRenderThread();
        /* Process OS/window event messages in this main thread */
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        /* Wait for the latch to signal that init render thread actions are done */
        runWndProcLoop();
        /*
         * After the wnd loop exited (because the window was closed), wait for render thread to complete
         * finalization.
         */
        updateAndRenderThread.join();
        if (debugProc != null) {
            debugProc.free();
        }
        glfwFreeCallbacks(gameWindow.getGlfwWindow());
        glfwDestroyWindow(gameWindow.getGlfwWindow());
        glfwTerminate();
    }

    private void initGLResources() {
        glfwMakeContextCurrent(gameWindow.getGlfwWindow());
        glfwSwapInterval(VSYNC ? 1 : 0);

        /* Determine, which additional OpenGL capabilities we have. */
        determineOpenGLCapabilities();

        /* Make sure everything is ready before we show the window */
        glFlush();
        glfwMakeContextCurrent(NULL);
        GL.setCapabilities(null);
    }

    /**
     * Loop in the main thread to only process OS/window event messages.
     * <p>
     */
    private void runWndProcLoop() {
        glfwShowWindow(gameWindow.getGlfwWindow());
        while (!glfwWindowShouldClose(gameWindow.getGlfwWindow())) {
            glfwWaitEvents();
        }
    }

    /**
     * Query all (optional) capabilites/extensions that we want to use from the OpenGL context via
     * LWJGL's {@link GLCapabilities}.
     */
    private void determineOpenGLCapabilities() {
        caps = GL.createCapabilities();

        // Set the clear color
        glClearColor(0f, 0f, 0f, 0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);

        if (SHOW_TRIANGLES) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        if (CULL_FACE) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK_LEFT);
        }
    }

    /**
     * Create a dedicated thread to process updates and perform rendering.
     * <p>
     * This is <em>only</em> for decoupling the render thread from potentially long-blocking
     * {@link org.lwjgl.glfw.GLFW#glfwPollEvents()} calls (when e.g. many mouse move events occur).
     * <p>
     */
    private Thread createAndStartUpdateAndRenderThread() {
        this.currentScene = new LevelScene();
        Thread renderThread = new Thread(this.currentScene::runUpdateAndRenderLoop);
        renderThread.setName("Render Thread");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        renderThread.start();
        return renderThread;
    }

}

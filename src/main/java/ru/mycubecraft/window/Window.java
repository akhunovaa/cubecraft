package ru.mycubecraft.window;

import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import ru.mycubecraft.Settings;
import ru.mycubecraft.engine.SceneLight;

import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.scene.LevelEditorScene;
import ru.mycubecraft.scene.LevelScene;
import ru.mycubecraft.scene.Scene;
import ru.mycubecraft.util.Time;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static Window instance;
    private final String title;
    private int width;
    private int height;
    private float red = 0.0f;
    private float green = 0.0f;
    private float blue = 0.0f;
    private float alpha = 1.0f;
    private long glfwWindow;
    private KeyboardListener keyboardListener;
    private MouseListener mouseListener;
    private Scene currentScene;
    private boolean resized;
    private final float skyBoxScale = 50.0f;

    private Window() {
        this.width = Settings.WIDTH;
        this.height = Settings.HEIGHT;
        this.title = Settings.WINDOW_TITLE;
    }

    public static synchronized Window getInstance() {
        if (instance == null) {
            instance = new Window();
        }
        return instance;
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
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden
        // after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window where glfwWindow stores the memory address of created window
        this.glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        // Setup resize callback
        glfwSetWindowSizeCallback(this.glfwWindow, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                Window.this.width = width;
                Window.this.height = height;
                Window.this.setResized(true);
            }
        });

        this.mouseListener = MouseListener.getInstance();
        glfwSetCursorPosCallback(this.glfwWindow, this.mouseListener::mousePositionCallback);
        glfwSetCursorEnterCallback(this.glfwWindow, this.mouseListener::mouseGetWindow);
        glfwSetMouseButtonCallback(this.glfwWindow, this.mouseListener::mouseButtonCallback);
        glfwSetScrollCallback(this.glfwWindow, this.mouseListener::mouseScrollCallback);

        this.keyboardListener = KeyboardListener.getInstance();
        glfwSetKeyCallback(this.glfwWindow, this.keyboardListener::keyCallback);


        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(this.glfwWindow, ((vidmode != null ? vidmode.width() : 800) - width) / 2, ((vidmode != null ? vidmode.height() : 600) - height) / 2);


        // Make the OpenGL context current
        glfwMakeContextCurrent(this.glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);
        // Make the window visible
        glfwShowWindow(this.glfwWindow);

        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        //glfwSetInputMode(this.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        this.changeScene(1);
        this.setupLights();
        currentScene.init();
    }

    public void loop() {
        float beginTime = Time.getTime();
        float endTime;
        float dt = -1.0f;
        while (!glfwWindowShouldClose(this.glfwWindow)) {

            // Poll events
            glfwPollEvents();

            glClearColor(this.red, this.green, this.blue, this.alpha);
            glClear(GL_COLOR_BUFFER_BIT);

            if (this.keyboardListener.isKeyPressed(GLFW_PRESS)) {
                glfwSetCursorPos(this.glfwWindow, (float) this.width / 2, (float) this.height / 2);
            }

            if (dt > 0) {
                currentScene.update(dt);
                currentScene.render();
            }

            glfwSwapBuffers(this.glfwWindow);

            endTime = Time.getTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

    public void changeScene(int scene) {
        switch (scene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                throw new IllegalStateException("Scene for load is wrong!");
        }
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        currentScene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(-1, 0, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

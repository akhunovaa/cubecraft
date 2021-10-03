package ru.mycubecraft.listener;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardListener {

    private static KeyboardListener instance;
    private final boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    private boolean fly;
    private boolean wireframe;
    private boolean debugBoundingBoxes;

    private KeyboardListener() {
    }

    public static synchronized KeyboardListener getInstance() {
        if (instance == null) {
            instance = new KeyboardListener();
        }
        return instance;
    }

    public void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW_PRESS) {
            this.keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            this.keyPressed[key] = false;
        }

        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
        }
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
        }
    }


    /**
     * GLFW callback when a key is pressed/released.
     */
    public void onKey(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(window, true);
        } else if (key >= 0) {
            keyPressed[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
        }
        handleSpecialKeys(key, action);
    }

    /**
     * Handle special keyboard keys before storing a key press/release state in the {@link #keyPressed}
     * array.
     */
    private void handleSpecialKeys(int key, int action) {
        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            fly = !fly;
        } else if (key == GLFW_KEY_2 && action == GLFW_PRESS) {
            wireframe = !wireframe;
        } else if (key == GLFW_KEY_1 && action == GLFW_PRESS) {
            debugBoundingBoxes = !debugBoundingBoxes;
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return this.keyPressed[keyCode];
    }


}

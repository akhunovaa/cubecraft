package ru.mycubecraft.listener;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class KeyboardListener {

    private static KeyboardListener instance;
    private final boolean[] keyPressed = new boolean[350];

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

    public boolean isKeyPressed(int keyCode) {
        return this.keyPressed[keyCode];
    }


}
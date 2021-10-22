package ru.mycubecraft.listener;

import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.glfw.GLFW.*;

@Getter
@Setter
public class MouseListener {

    private static MouseListener instance;
    private final boolean[] mouseButtonPressed = new boolean[3];
    private final boolean inWindow = false;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    private boolean firstCursorPos = true;
    private int mouseX, mouseY;
    private float angx, angy, dangx, dangy;

    private MouseListener() {
    }

    public static synchronized MouseListener getInstance() {
        if (instance == null) {
            instance = new MouseListener();
        }
        return instance;
    }

    /**
     * Callback for mouse movement.
     *
     * @param window the window (we only have one, currently)
     * @param x      the x coordinate
     * @param y      the y coordinate
     */
    public void onCursorPos(long window, double x, double y) {
        if (!firstCursorPos) {
            float deltaX = (float) x - mouseX;
            float deltaY = (float) y - mouseY;
            dangx += deltaY;
            dangy += deltaX;
        }
        firstCursorPos = false;
        mouseX = (int) x;
        mouseY = (int) y;
    }


    /**
     * GLFW callback for mouse buttons.
     */
    public void onMouseButton(long window, int button, int action, int mods) {
        leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
        rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
    }


    public boolean isKeyPressed(int keyCode) {
        return this.mouseButtonPressed[keyCode];
    }
}

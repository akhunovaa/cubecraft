package ru.mycubecraft.listener;

import ru.mycubecraft.data.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {

    private static MouseListener instance;
    private final boolean[] mouseButtonPressed = new boolean[3];
    private double scrollX;
    private double scrollY;
    private double xPosition, yPosition, xLastPosition, yLastPosition;
    private boolean dragging;
    private boolean inWindow = false;

    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPosition = 0.0;
        this.yPosition = 0.0;
        this.xLastPosition = 0.0;
        this.yLastPosition = 0.0;
    }

    public static synchronized MouseListener getInstance() {
        if (instance == null) {
            instance = new MouseListener();
        }
        return instance;
    }

    public void endFrame() {
        this.scrollX = 0;
        this.scrollY = 0;
        this.xLastPosition = this.xPosition;
        this.yLastPosition = this.yPosition;
    }

    public boolean mouseButtonDown(int button) {
        if (button < this.mouseButtonPressed.length) {
            return this.mouseButtonPressed[button];
        } else {
            return false;
        }
    }

    public void mouseGetWindow() {
        this.inWindow = true;
    }

    public boolean isMouseOnWindow() {
        return this.inWindow;
    }

    public void mousePositionCallback(long window, double xPosition, double yPosition) {
        this.xLastPosition = instance.xPosition;
        this.yLastPosition = instance.yPosition;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.dragging = this.mouseButtonPressed[0] || this.mouseButtonPressed[1] || this.mouseButtonPressed[2];
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (button < this.mouseButtonPressed.length) {
                this.mouseButtonPressed[button] = true;
            }
        } else if (action == GLFW_RELEASE) {
            if (button < this.mouseButtonPressed.length) {
                this.mouseButtonPressed[button] = false;
                this.dragging = false;
            }
        }
    }

    public void mouseScrollCallback(long window, double xOffset, double yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
    }

    public float getScrollX() {
        return (float) scrollX;
    }

    public float getScrollY() {
        return (float) scrollY;
    }

    public float getxPosition() {
        return (float) xPosition;
    }

    public float getyPosition() {
        return (float) yPosition;
    }

    public boolean isDragging() {
        return dragging;
    }

    public float getDx() {
        return (float) (xLastPosition - xPosition);
    }

    public float getDy() {
        return (float) (yLastPosition - yPosition);
    }

    public double getxLastPosition() {
        return xLastPosition;
    }

    public double getyLastPosition() {
        return yLastPosition;
    }

    public boolean isInWindow() {
        return inWindow;
    }

    public void mouseGetWindow(long l, boolean inWindow) {
        this.inWindow = inWindow;
    }
}

package ru.mycubecraft.listener;

import org.joml.Vector2f;

public class MouseListener {

    private static MouseListener instance;
    private final boolean[] mouseButtonPressed = new boolean[3];
    private final Vector2f displVec;
    private final double xPosition;
    private final double yPosition;
    private final double xLastPosition;
    private final double yLastPosition;
    private final boolean inWindow = false;

    private boolean firstCursorPos = true;
    private int mouseX, mouseY;
    private float angx, angy, dangx, dangy;
    private boolean hasSelection;

    private MouseListener() {
        this.xPosition = 0.0;
        this.yPosition = 0.0;
        this.xLastPosition = 0.0;
        this.yLastPosition = 0.0;
        this.displVec = new Vector2f();
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
        // updateAndRenderRunnables.add(new DelayedRunnable(() -> {
//            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS && hasSelection) {
//                //placeAtSelectedVoxel();
//            } else if (button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS && hasSelection) {
//                //removeSelectedVoxel();
//            }
//            return null;
//        }, "Mouse button event", 0));
    }
}

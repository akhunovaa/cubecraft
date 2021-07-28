package ru.mycubecraft;

public final class Settings {

    public static final String WINDOW_TITLE = "Cubecraft Game Engine in JAVA | Developed by Azat Akhunov";

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    public static final boolean SHOW_TRIANGLES = false;
    public static final boolean CULL_FACE = true;
    public static final boolean ANTIALIASING = true;

    public static final float MOVE_SPEED = 0.25f;

    public static final float FOV = (float) Math.toRadians(60.0f);
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000.0f;

    public static final float MOUSE_SENSITIVITY = 0.095f;

    public static final float MAX_LOOK = 90.0f;
    public static final float MIN_LOOK = -90.0f;

    public static final float SKY_BOX_SCALE = 100;

}

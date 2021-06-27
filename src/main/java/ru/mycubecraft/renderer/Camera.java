package ru.mycubecraft.renderer;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    public static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 100.f;

    private final Transformation transformation;
    public Vector4f position;
    public Vector3f rotation;

    public Camera() {
        this.transformation = new Transformation();
        position = new Vector4f(0, 0, 0, FOV);
        rotation = new Vector3f(0, 0, 0);
    }

    public Vector4f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }


    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }
}

package ru.mycubecraft.renderer;

import org.joml.Vector3f;

public class Camera {

    private static final float MAX_LOOK = 85;
    private static final float MOUSE_SENSITIVITY = 0.55f;

    public final Transformation transformation;

    public Camera() {
        this.transformation = new Transformation();
    }

    public Camera(Transformation transformation) {
        this.transformation = transformation;
    }

    public Vector3f getRotation() {
        return this.transformation.rotation;
    }

    public void rotateCamera(float mouseDx, float mouseDy, float delta) {
        float y = Math.max(-MAX_LOOK, Math.min(MAX_LOOK, mouseDx * MOUSE_SENSITIVITY * delta));
        this.transformation.rotation.add(mouseDy * MOUSE_SENSITIVITY * delta, y, 0.0f);
        if (y > MAX_LOOK) {
            System.out.println("xRotation: " + this.transformation.rotation.y);
        }
    }
}

package ru.mycubecraft.renderer;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {

    public Vector4f position;
    public Vector3f rotation;

    public Camera() {
        position = new Vector4f(0, 0, 0, 100.0f);
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

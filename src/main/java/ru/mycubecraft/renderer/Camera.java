package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.Settings;
import ru.mycubecraft.util.MathUtil;

public class Camera {

    private static final float CAMERA_POS_STEP = 0.65f;
    private final Matrix4f viewMatrix;
    public Vector4f position;
    public Vector3f rotation;

    public Camera() {
        this.position = new Vector4f(29.0f, 180.0f, 29.0f, 1);
        this.rotation = new Vector3f(32.0f, 9.0f, 0.0f);
        viewMatrix = new Matrix4f();
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f updateViewMatrix() {
        return Transformation.updateGenericViewMatrix(new Vector3f(position.x, position.y, position.z), rotation, viewMatrix);
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

    public void moveRotation(double offsetX, double offsetY, double offsetZ) {
        double xRotation = rotation.x;
        double yRotation = rotation.y;
        yRotation += offsetY;
        if (yRotation > 360.0f || yRotation < -360.0f) {
            yRotation = 0.0f;
        }
        xRotation += offsetX;
        rotation.y = (float) yRotation;
        rotation.z += offsetZ;
        rotation.x = MathUtil.clamp((float) xRotation, (float) Settings.MIN_LOOK, (float) Settings.MAX_LOOK);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }

}

package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private final Matrix4f projectionMatrix;
    private final Matrix4f worldMatrix;

    public Transformation() {
        worldMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
    }

    public final Matrix4f getCameraProjectionMatrix(float fov, float width, float height, float zNear, float zFar, Camera c) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.transform(c.getPosition());
        projectionMatrix.rotateY(c.getRotation().y);
        projectionMatrix.rotateX(c.getRotation().x);
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);

        return projectionMatrix;
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);

        return projectionMatrix;
    }

    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        worldMatrix.identity().translate(offset).
                rotateX((float) Math.toRadians(rotation.x)).
                rotateY((float) Math.toRadians(rotation.y)).
                rotateZ((float) Math.toRadians(rotation.z)).
                scale(scale);
        return worldMatrix;
    }

    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale, Camera c) {
        worldMatrix.identity().rotateX(-(float) Math.toRadians(c.rotation.x)).
                rotateY(-(float) Math.toRadians(c.rotation.y)).
                translate(new Vector3f(c.getPosition().x, -c.getPosition().y, c.getPosition().z)).
                translate(offset).
                rotateX((float) Math.toRadians(rotation.x)).
                rotateY((float) Math.toRadians(rotation.y)).
                rotateZ((float) Math.toRadians(rotation.z)).
                scale(scale);
        return worldMatrix;
    }
}

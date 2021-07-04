package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Transformation {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 60.0f;

    private final Matrix4f projectionMatrix;
    private final Matrix4f worldMatrix;

    public Vector4f position;
    public Vector3f rotation;

    public Transformation() {
        this.worldMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.position = new Vector4f();
        this.rotation = new Vector3f();
    }

    public void addPosition(Vector3f vector3f) {
        position.add(new Vector4f(vector3f, FOV));
    }

    public void addRotation(Vector3f vector3f) {
        rotation.add(vector3f);
    }

    public final Matrix4f getCameraProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        this.projectionMatrix.identity();
        this.projectionMatrix.transform(this.position);
        this.projectionMatrix.rotateY(this.rotation.y);
        this.projectionMatrix.rotateX(this.rotation.x);
        this.projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);

        return this.projectionMatrix;
    }

    public final Matrix4f getProjectionMatrix(float width, float height) {
        float aspectRatio = width / height;
        this.projectionMatrix.identity();
        this.projectionMatrix.perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);

        return this.projectionMatrix;
    }

    public Matrix4f getModelMatrix(Vector3f offset, Vector3f rotation, float scale) {
        this.worldMatrix.identity()
                .translate(offset)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
        return this.worldMatrix;
    }

    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        this.worldMatrix.identity()
                .rotateX(-(float) Math.toRadians(this.rotation.x))
                .rotateY(-(float) Math.toRadians(this.rotation.y))
                .translate(new Vector3f(this.position.x, -this.position.y, this.position.z))
                .translate(offset)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
        return this.worldMatrix;
    }
}

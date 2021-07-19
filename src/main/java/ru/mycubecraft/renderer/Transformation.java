package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Settings;

public class Transformation {

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f orthoMatrix;
    private final Matrix4f orthoModelMatrix;

    public Transformation() {
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
        this.modelViewMatrix = new Matrix4f();
        this.orthoMatrix = new Matrix4f();
        this.orthoModelMatrix = new Matrix4f();
    }

    public Matrix4f updateProjectionMatrix(float width, float height) {
        projectionMatrix.identity();
        return projectionMatrix.setPerspective(Settings.FOV, width / height, Settings.Z_NEAR, Settings.Z_FAR);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }


    public Matrix4f updateViewMatrix(Camera camera) {
        Vector4f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();
        // First do the rotation so camera rotates over its position
        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        // Then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getModelMatrix(Vector3f offset, Vector3f rotation, float scale) {
        this.modelMatrix.identity()
                .translate(offset)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
        return this.modelMatrix;
    }

    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity()
                .translate(gameItem.getPosition())
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .scale(gameItem.getScale());
        modelViewMatrix.set(viewMatrix);
        return modelViewMatrix.mul(modelMatrix);
    }

    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }


    public Matrix4f buildOrthoProjModelMatrix(GameItem gameItem, Matrix4f orthoMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float) Math.toRadians(-rotation.x)).
                rotateY((float) Math.toRadians(-rotation.y)).
                rotateZ((float) Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelMatrix);
        return orthoModelMatrix;
    }

}

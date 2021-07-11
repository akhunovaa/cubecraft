package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.Settings;
import ru.mycubecraft.engine.graph.Camera;

public class Transformation {

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelViewMatrix;

    public Transformation() {
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
        this.modelViewMatrix = new Matrix4f();
    }


    public final Matrix4f getProjectionMatrix(float width, float height) {
        float aspectRatio = width / height;
        this.projectionMatrix.identity();
        this.projectionMatrix.perspective(Settings.FOV, aspectRatio, Settings.Z_NEAR, Settings.Z_FAR);

        return this.projectionMatrix;
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
    public Matrix4f getSkyModelMatrix(Vector3f offset, Vector3f rotation, float scale) {
        modelMatrix.identity()
                .translate(offset)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .scale(scale);
        return this.modelMatrix;
    }

    public Matrix4f getViewMatrix(Camera camera) {
        this.viewMatrix.identity()
                .rotateX(-(float) Math.toRadians(camera.getRotation().x))
                .rotateY(-(float) Math.toRadians(camera.getRotation().y))
                .translate(new Vector3f(camera.getPosition().x, -camera.getPosition().y, camera.getPosition().z));
        return this.viewMatrix;
    }

    public Matrix4f getSkyViewMatrix(Camera camera) {
        this.viewMatrix.identity()
                .rotateX(-(float) Math.toRadians(camera.getRotation().x))
                .rotateY(-(float) Math.toRadians(camera.getRotation().y))
                .translate(new Vector3f(camera.getPosition().x, -camera.getPosition().y, camera.getPosition().z));
        return this.viewMatrix;
    }

    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition())
                .rotateX(-(float)Math.toRadians(rotation.x))
                .rotateY(-(float)Math.toRadians(rotation.y))
                .rotateZ(-(float)Math.toRadians(rotation.z))
                .scale(gameItem.getScale());
        modelViewMatrix.set(viewMatrix);
        return modelViewMatrix.mul(modelMatrix);
    }

}

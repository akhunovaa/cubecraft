package ru.mycubecraft.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.window.Window;

import java.util.List;

public class MouseBoxSelectionDetector extends CameraBoxSelectionDetector {

    private final Matrix4f invProjectionMatrix;
    private final Matrix4f invViewMatrix;

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    public MouseBoxSelectionDetector() {
        super();
        invProjectionMatrix = new Matrix4f();
        invViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public void update(Camera camera) {
        viewMatrix = camera.updateViewMatrix();
        projectionMatrix = transformation.updateProjectionMatrix(window.getWidth(), window.getHeight());
    }

    public Vector3f getGameItemPosition(List<GameItem> gameItemList, Camera camera) {

        float xStart = camera.getPosition().x;
        float yStart = camera.getPosition().y;
        float zStart = camera.getPosition().z;

//        float xStart = (float) Math.floor(camera.getPosition().x);
//        float yStart = (float) Math.floor(camera.getPosition().y);
//        float zStart = (float) Math.floor(camera.getPosition().z);

        Vector3f origin = new Vector3f(xStart, yStart, zStart);
        Vector3f cameraDir = rayDirection();

        Vector3f newGameItemPosition = new Vector3f();

        GameItem selectedGameItem = selectGameItem(gameItemList, origin, cameraDir);
        if (selectedGameItem != null) {
            newGameItemPosition.set(selectedGameItem.getPosition());
        }
        return newGameItemPosition;
    }

    public Vector3f getGameItemPosition(Camera camera) {

        float xStart = (float) Math.ceil(camera.getPosition().x);
        float yStart = (float) Math.floor(camera.getPosition().y);
        float zStart = (float) Math.ceil(camera.getPosition().z);

        Vector3f origin = new Vector3f(xStart, yStart, zStart);

        Vector4f position = new Vector4f();
        Window window = Window.getInstance();

        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight();

        float x = (float) 2 * (wdwWidth / 2f) / wdwWidth - 1.0f;
        float y = 1.0f - (float) 2 * (wdwHeight / 2f) / wdwHeight - 100;
        float z = -1.0f;

        invProjectionMatrix.set(projectionMatrix);
        invProjectionMatrix.invert();

        position.set(x, y, z, 1.0f);
        position.mul(invProjectionMatrix);

        position.z = -1.0f;
        position.w = 0.0f;

        invViewMatrix.set(viewMatrix);
        invViewMatrix.invert();

        position.mul(invViewMatrix);

        position.x *= 6;
        position.y *= 6;
        position.z *= 6;

        position.x += origin.x;
        position.y += origin.y;
        position.z += origin.z;

        return new Vector3f(position.x, position.y, position.z);
    }

    public Vector3f rayDirection() {
        Vector4f position = new Vector4f();
        Window window = Window.getInstance();
        Vector3f cameraDir = new Vector3f();
        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight();

        float x = 2f * (wdwWidth / 2f) / wdwWidth - 1.0f;
        float y = 1.0f - 2f * (wdwHeight / 2f) / wdwHeight;
        float z = 0.0f;

        invProjectionMatrix.set(projectionMatrix);
        invProjectionMatrix.invert();

        position.set(x, y, z, 0.0f);
        position.mul(invProjectionMatrix);
        position.z = -1.0f;
        position.w = 0.0f;

        invViewMatrix.set(viewMatrix);
        invViewMatrix.invert();

        position.mul(invViewMatrix);

        cameraDir.set(position.x, position.y, position.z);
        return cameraDir;

    }

}

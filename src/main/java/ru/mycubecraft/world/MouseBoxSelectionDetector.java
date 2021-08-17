package ru.mycubecraft.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.window.Window;

import java.util.ArrayList;
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

    public Vector3f getGameItemPosition(List<GameItem> instancedGameItems, List<GameItem> generatedGameItems, Camera camera) {
        float xStart = (float) Math.ceil(camera.getPosition().x);
        float yStart = (float) Math.floor(camera.getPosition().y);
        float zStart = (float) Math.ceil(camera.getPosition().z);

        Vector3f origin = new Vector3f(xStart, yStart, zStart);

        List<GameItem> items = new ArrayList<>(instancedGameItems);
        items.addAll(generatedGameItems);

        Vector4f position = new Vector4f();
        Window window = Window.getInstance();

        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight() - 100;

        float x = (float) 2 * (wdwWidth / 2f) / wdwWidth - 1.0f;
        float y = 1.0f - (float) 2 * (wdwHeight / 2f) / wdwHeight;
        float z = -1.0f;

        invProjectionMatrix.set(projectionMatrix);
        invProjectionMatrix.invert();

        position.set(x, y, z, 1.0f);
        position.mul(invProjectionMatrix);
        position.z = -1.0f;

        invViewMatrix.set(viewMatrix);
        invViewMatrix.invert();
        position.mul(invViewMatrix);

        position.x *= 6;
        position.y *= 6;
        position.z *= 6;

        position.x += origin.x;
        position.y += origin.y;
        position.z += origin.z;

        Vector3f newGameItemPosition = null;

        GameItem selectedGameItem = selectGameItem(items, origin, new Vector3f(position.x, position.y, position.z).normalize());
        if (selectedGameItem != null) {
            //Vector4f subbed = new Vector4f(selectedGameItem.getPosition(), 0.0f).sub(position);
            Vector4f subbed = position.sub(new Vector4f(selectedGameItem.getPosition(), 0.0f));
            newGameItemPosition = new Vector3f(subbed.x, subbed.y, subbed.z);
        }
        items.clear();
        return newGameItemPosition;
    }

    public Vector3f getGameItemPosition(Camera camera) {

        float xStart = (float)Math.ceil(camera.getPosition().x);
        float yStart = (float)Math.floor(camera.getPosition().y);
        float zStart = (float)Math.ceil(camera.getPosition().z);

        Vector3f origin = new Vector3f(xStart, yStart, zStart);

        Vector4f position = new Vector4f();
        Window window = Window.getInstance();

        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight() - 100;

        float x = (float) 2 * (wdwWidth / 2f) / wdwWidth - 1.0f;
        float y = 1.0f - (float) 2 * (wdwHeight / 2f) / wdwHeight;
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
}

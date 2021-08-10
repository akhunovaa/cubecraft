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

    private final Vector3f mouseDir;

    private final Vector4f tmpVec;

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    public MouseBoxSelectionDetector() {
        super();
        invProjectionMatrix = new Matrix4f();
        invViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        mouseDir = new Vector3f();
        tmpVec = new Vector4f();
    }

    public void update(Camera camera) {
        viewMatrix = camera.updateViewMatrix();
        projectionMatrix = transformation.updateProjectionMatrix(window.getWidth(), window.getHeight());
    }

    public Vector3f getGameItemPosition(List<GameItem> instancedGameItems, List<GameItem> generatedGameItems, Camera camera) {

        List<GameItem> items = new ArrayList<>(instancedGameItems);
        items.addAll(generatedGameItems);

        Window window = Window.getInstance();

        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight() - 100;

        float x = (float) 2 * (wdwWidth / 2f) / wdwWidth - 1.0f;
        float y = 1.0f - (float) 2 * (wdwHeight / 2f) / wdwHeight;
        float z = -1.0f;

        invProjectionMatrix.set(projectionMatrix);
        invProjectionMatrix.invert();

        tmpVec.set(x, y, z, 1.0f);
        tmpVec.mul(invProjectionMatrix);
        tmpVec.z = -1.0f;
        tmpVec.w = 0.0f;

        invViewMatrix.set(viewMatrix);
        invViewMatrix.invert();
        tmpVec.mul(invViewMatrix);

        mouseDir.set(tmpVec.x, tmpVec.y, tmpVec.z);

        Vector3f selectedItemPosition = null;

        Vector3f camPosition = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        GameItem selectedGameItem = selectGameItem(items, camPosition, mouseDir);
        if (selectedGameItem != null) {
            selectedItemPosition = selectedGameItem.getPosition();
        }
        items.clear();
        return selectedItemPosition;
    }
}

package ru.mycubecraft.world;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.Intersectionf;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Transformation;
import ru.mycubecraft.window.Window;

import java.util.List;

public class CameraBoxSelectionDetector {

    private final Vector3f max;
    private final Vector3f min;
    private final Vector2f nearFar;
    private final Vector3f dir;
    protected Transformation transformation = Transformation.getInstance();
    protected Window window = Window.getInstance();

    public CameraBoxSelectionDetector() {
        dir = new Vector3f();
        min = new Vector3f();
        max = new Vector3f();
        nearFar = new Vector2f();
    }

    public GameItem selectGameItem(List<GameItem> gameItems, Vector3f camPosition, Vector3f mouseDir) {

        GameItem selectedGameItem = null;
        float closestDistance = 20.0f;

        int idx = 0;

        for (int i = 0; i < gameItems.size(); i++) {
            GameItem gameItem = gameItems.get(i);
            gameItem.setSelected(false);

            Vector3f gamePosition = new Vector3f(gameItem.getPosition());
            min.set(gamePosition);
            max.set(gamePosition);
            min.add(-gameItem.getScale() / 2, -gameItem.getScale() / 2, -gameItem.getScale() / 2);
            max.add(gameItem.getScale() / 2, gameItem.getScale() / 2, gameItem.getScale() / 2);
            if (Intersectionf.intersectRayAab(camPosition, mouseDir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                idx = i;
                selectedGameItem = gameItem;
            }
        }
        if (selectedGameItem != null) {
            selectedGameItem.setSelected(true);
            gameItems.set(idx, selectedGameItem).setSelected(true);
        }
        return selectedGameItem;
    }
}

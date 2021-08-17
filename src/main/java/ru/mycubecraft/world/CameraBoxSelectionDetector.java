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

    public GameItem selectGameItem(List<GameItem> gameItems, Vector3f camPosition, Vector3f dir) {

        GameItem selectedGameItem = null;
        float closestDistance = 20.0f;

        for (GameItem gameItem : gameItems) {
            min.set(gameItem.getPosition());
            max.set(gameItem.getPosition());
            min.add(-gameItem.getScale(), -gameItem.getScale(), -gameItem.getScale());
            max.add(gameItem.getScale(), gameItem.getScale(), gameItem.getScale());

            if (Intersectionf.intersectRayAab(camPosition, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                selectedGameItem = gameItem;
            }
        }

        return selectedGameItem;
    }
}

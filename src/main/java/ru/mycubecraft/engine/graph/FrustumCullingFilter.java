package ru.mycubecraft.engine.graph;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.Intersectionf;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Camera;

import java.util.ArrayList;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;
    private final Vector2f nearFar;
    private float closestDistance = Settings.Z_FAR;
    private final FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
        nearFar = new Vector2f();
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }

    public void filter(ArrayList<GameItem> gameItems, Camera camera) {
        gameItems.parallelStream().forEach(item -> filter(item, camera));
    }

    public void filter(GameItem gameItem, Camera camera) {
        float boundingRadius;
        Vector3f gameItemPosition;
        if (gameItem != null && !gameItem.isDisableFrustumCulling()) {
            boundingRadius = gameItem.getScale() * gameItem.getMesh().getBoundingRadius();
            gameItemPosition = gameItem.getPosition();
            boolean insideTheFrustum = insideFrustum(gameItemPosition.x, gameItemPosition.y, gameItemPosition.z, boundingRadius);
            Vector3f min = new Vector3f();
            Vector3f max = new Vector3f();
            min.set(gameItem.getPosition());
            max.set(gameItem.getPosition());
            min.add(-10, -10, -10);
            max.add(10, 10, 10);
            float xStart = camera.getPosition().x;
            float yStart = camera.getPosition().y;
            float zStart = camera.getPosition().z;

            Vector3f origin = new Vector3f(xStart, yStart, zStart);
            if (Intersectionf.intersectRayAab(origin, origin.normalize(), min, max, nearFar) && nearFar.x < closestDistance) {
                gameItem.setInsideFrustum(insideTheFrustum);
            }
        }
    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }

    public boolean insideFrustum(float x0, float y0, float z0) {
        return frustumInt.testPoint(x0, y0, z0);
    }
}

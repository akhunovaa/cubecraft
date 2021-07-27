package ru.mycubecraft.engine.graph;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;

    private final FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }

    public void filter(ArrayList<GameItem> gameItems) {
        for (GameItem gameItem : gameItems) {
            float meshBoundingRadius = gameItem.getMesh().getBoundingRadius();
            filter(gameItem, meshBoundingRadius);
        }
    }

    public void filter(GameItem gameItem, float meshBoundingRadius) {
        float boundingRadius;
        Vector3f pos;
        if (!gameItem.isDisableFrustumCulling()) {
            boundingRadius = gameItem.getScale() * meshBoundingRadius;
            pos = gameItem.getPosition();
            gameItem.setInsideFrustum(insideFrustum(pos.x, pos.y, pos.z, boundingRadius));
        }

    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }
}

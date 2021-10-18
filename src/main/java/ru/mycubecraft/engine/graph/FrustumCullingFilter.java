package ru.mycubecraft.engine.graph;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;

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


    public void filter(GameItem gameItem) {
        float boundingRadius;
        Vector3f gameItemPosition;
        if (gameItem != null) {
            boundingRadius = gameItem.getScale() * gameItem.getMesh().getBoundingRadius();
            gameItemPosition = gameItem.getPosition();
            boolean insideTheFrustum = insideFrustum(gameItemPosition.x, gameItemPosition.y, gameItemPosition.z, boundingRadius);
            gameItem.setInsideFrustum(insideTheFrustum);
        }
    }

    public boolean filter(Block block) {
        float boundingRadius = block.getScale() * block.getBoundingRadius();
        Vector3f blockPosition = block.getPosition();
        boolean insideTheFrustum = insideFrustum(blockPosition.x, blockPosition.y, blockPosition.z, boundingRadius);
        block.setInsideFrustum(insideTheFrustum);
        return insideTheFrustum;
    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }

    public boolean insideFrustum(float x0, float y0, float z0) {
        return frustumInt.testPoint(x0, y0, z0);
    }

    public int intersectSphere(float x0, float y0, float z0, float radius) {
        return frustumInt.intersectSphere(x0, y0, z0, radius);
    }
}

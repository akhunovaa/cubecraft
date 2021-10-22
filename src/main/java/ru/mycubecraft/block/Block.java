package ru.mycubecraft.block;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.cube.Cube;

@Getter
@Setter
public abstract class Block {

    final Vector3f position;
    boolean disableFrustumCulling;
    boolean visible;
    boolean selected;
    float scale;
    float boundingRadius;
    private boolean insideFrustum;

    public Block(int bX, int bY, int bZ) {
        this.position = new Vector3f(bX, bY, bZ);
        this.disableFrustumCulling = false;
        this.insideFrustum = false;
        this.scale = 1.0f;
        this.boundingRadius = 1.0f;
    }

    public Block(Vector3f position) {
        this.position = position;
    }

    public abstract void createCube(Cube cube);

    public abstract void render();

    public abstract GameItem getGameCubeItem();
}

package ru.mycubecraft.block;

import lombok.Getter;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Cube;

@Getter
public class Block {

    private final Vector3f position;
    private GameItem gameCubeItem;

    public Block(int bX, int bY, int bZ, String texture) {
        this.position = new Vector3f(bX, bY, bZ);
        try {
            Cube gameCubeItem = new Cube(new Vector3f(1.0f), texture);
            gameCubeItem.setPosition(bX, bY, bZ);
            this.gameCubeItem = gameCubeItem;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Block(int bX, int bY, int bZ) {
        this(bX, bY, bZ, "assets/textures/white.png");
    }

    public void render() {
        gameCubeItem.render();
    }

}

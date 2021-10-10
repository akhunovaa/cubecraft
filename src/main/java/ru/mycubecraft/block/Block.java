package ru.mycubecraft.block;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Cube;

@Getter
@Setter
public class Block {

    private final Vector3f position;
    private GameItem gameCubeItem;
    private boolean visible;

    public Block(int bX, int bY, int bZ, String texture) {
        this.position = new Vector3f(bX, bY, bZ);
        try {
            this.gameCubeItem = new Cube(texture);
            this.gameCubeItem.setPosition(bX, bY, bZ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Block(Vector3f position, String texture) {
        this.position = position;
        try {
            Cube gameCubeItem = new Cube(texture);
            gameCubeItem.setPosition(position.x, position.y, position.z);
            this.gameCubeItem = gameCubeItem;
        } catch (Exception e) {
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

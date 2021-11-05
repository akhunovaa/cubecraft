package ru.mycubecraft.block;

import lombok.Getter;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.cube.Cube;

@Getter
public class GrassBlock extends Block {

    private final static String MODEL_TEXTURE_PATH = "assets/textures/grass.png";

    private GameItem gameCubeItem;

    public GrassBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ);
    }

    @Override
    public void createCube(Cube cube) {
        try {
            cube.setPosition(position.x, position.y, position.z);
            cube.setTexture(MODEL_TEXTURE_PATH);
            cube.createCube();
            this.gameCubeItem = cube;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        gameCubeItem.render();
    }

    @Override
    public GameItem getGameCubeItem() {
        if (this.gameCubeItem != null) {
            this.gameCubeItem.setSelected(this.selected);
        }
        return this.gameCubeItem;
    }


}

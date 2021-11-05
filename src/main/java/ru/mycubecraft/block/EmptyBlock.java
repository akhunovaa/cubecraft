package ru.mycubecraft.block;

import lombok.Getter;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.cube.Cube;

@Getter
public class EmptyBlock extends Block {

    private final static String MODEL_TEXTURE_PATH = "assets/textures/white.png";

    private GameItem gameCubeItem;

    public EmptyBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ);
        visible = false;
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

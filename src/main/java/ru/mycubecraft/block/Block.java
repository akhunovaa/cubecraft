package ru.mycubecraft.block;

import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Cube;

public class Block {
    private final Vector3f position;
    public GameItem model;

    public Block(int bX, int bY, int bZ, String texture) {
        position = new Vector3f(bX, bY, bZ);
        try {
            Cube cube = new Cube(new Vector3f(1.0f), texture);
            cube.setPosition(bX, bY, bZ);
            model = cube;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Block(int bX, int bY, int bZ) {

        this(bX, bY, bZ, "assets/textures/white.png");


    }

    public void render() {
        model.render();
    }
}

package ru.mycubecraft.scene;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.player.Player;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Cube;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class LevelScene extends Scene {

    public LevelScene() {
        System.out.println("Entered to a Level Scene");

        renderer = new Renderer();
        camera = new Camera();
        player = new Player();

        world = new World(new BasicGen(1));

        System.out.println("WORLD GEND");

        GameItem gameItem = new Cube(new Vector3f(1.0f, 1.0f, 1.0f), "assets/textures/dirt.png");
        gameItem.setPosition(0, 5, 0);

        gameItems.add(gameItem);


    }

    @Override
    public void update(float delta) {
        //System.out.println("Current FPS: " + (1.0f / dt) + " ");

        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(delta);
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackward(delta);
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(delta);
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(delta);
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.jump(delta);
        } else if (keyboardListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || keyboardListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            camera.sitDown(delta);
        }


        float cursorPositionX = mouseListener.getxPosition();
        float cursorPositionLastX = (float) mouseListener.getxLastPosition();
        float cursorPositionDx = mouseListener.getDx();

        float cursorPositionY = mouseListener.getyPosition();
        float cursorPositionLastY = (float) mouseListener.getyLastPosition();
        float cursorPositionDy = mouseListener.getDy();

        if (cursorPositionX != cursorPositionLastX && cursorPositionY != cursorPositionLastY && cursorPositionDx != 0.0f && cursorPositionDy != 0.0f) {
            camera.rotateCamera(cursorPositionDx, cursorPositionDy, delta);
        }


    }

    @Override
    public void render() {
        world.generate();
        renderer.render(window, gameItems, world, camera);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        world.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}

package ru.mycubecraft.scene;

import org.lwjgl.glfw.GLFW;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.data.Settings;
import ru.mycubecraft.player.Player;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class LevelScene extends Scene {

    private static final float CAMERA_POS_STEP = 0.05f;
    private Hud hud;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        renderer = new Renderer();
        camera = new Camera();
        world = new World(new BasicGen(1));
    }

    @Override
    public void init() {
        System.out.println("Entering To Word");
        hud = new Hud();
    }

    @Override
    public void update(float delta) {
        hud.rotateCompass(-camera.getRotation().y);
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

        boolean isOnWindow = mouseListener.isInWindow();

        if (isOnWindow && cursorPositionX != cursorPositionLastX && cursorPositionY != cursorPositionLastY && cursorPositionDx != 0.0f && cursorPositionDy != 0.0f) {
            camera.rotateCamera(cursorPositionDx, cursorPositionDy, delta);
        }

    }

    @Override
    public void render(float delta) {
        int xPosition = (int) camera.getPosition().x;
        int zPosition = (int) camera.getPosition().z;
        world.generate(xPosition, zPosition);
        skyBox.setScale(Settings.SKY_BOX_SCALE);
        hud.updateHud(window, camera);
        hud.updateFps(delta);
        renderer.render(window, gameItems, world, camera, skyBox, this, hud);
        //renderer.renderScene(window, camera, this);

    }

    @Override
    public void cleanup() {
        hud.cleanup();
        renderer.cleanup();
        world.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }

}

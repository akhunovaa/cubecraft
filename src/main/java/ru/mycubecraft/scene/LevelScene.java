package ru.mycubecraft.scene;

import org.joml.*;
import org.lwjgl.glfw.GLFW;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Cube;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.util.MathUtil;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;

public class LevelScene extends Scene {

    private final Quaternionf orientation = new Quaternionf();
    private final boolean changingScene = false;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        camera.position.add(2f, 15f, 0, FOV);
        camera.rotation.add(0, 0.0f, 0);
        renderer = new Renderer();
        world = new World(new BasicGen(1));

        System.out.println("WORLD GEND");

        if (world == null) {
            for (float y = -5.0f; y <= 0.0f; y++) {
                for (float x = -7.0f; x <= 8.0f; x++) {
                    for (float z = -7.0f; z <= 8.0f; z++) {
                        GameItem gameItem = new Cube(new Vector3f(1.0f, 1.0f, 1.0f), y < 0.0 ? "assets/textures/dirt.png" : "assets/textures/grass.png");
                        gameItem.setPosition(x, y, z);
                        gameItems.add(gameItem);
                    }
                }
            }
        } else {
            GameItem gameItem = new Cube(new Vector3f(1.0f, 1.0f, 1.0f), "assets/textures/dirt.png");
            gameItem.setPosition(0, 5, 0);

            gameItems.add(gameItem);
        }

    }

    @Override
    public void update(float dt) {
        //System.out.println("Current FPS: " + (1.0f / dt) + " ");
        float cameraSpeed = 3.0f;

        Vector4f cameraForward = new Vector4f(0f, 0f, dt * cameraSpeed, FOV);
        Vector4f cameraBackward = new Vector4f(0f, 0f, -dt * cameraSpeed, FOV);

        Vector4f cameraLeft = new Vector4f(dt * cameraSpeed, 0f, 0f, FOV);
        Vector4f cameraRight = new Vector4f(-dt * cameraSpeed, 0f, 0f, FOV);

        Vector4f cameraUp = new Vector4f(0f, dt * cameraSpeed, 0f, FOV);
        Vector4f cameraDown = new Vector4f(0f, -dt * cameraSpeed, 0f, FOV);


        float x = camera.position.x;
        float y = camera.position.y;
        float z = camera.position.z;


        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            camera.position = camera.getPosition().add(new Vector4f(0f, 0f, -dt * cameraSpeed, FOV).rotate(
                    new Quaternionf().setEulerAnglesXYZ(0.0f,
                            -(float) Math.toRadians(camera.getRotation().y), 0.0f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            camera.position = camera.getPosition().add(new Vector4f(0f, 0f, dt * cameraSpeed, FOV).rotate(
                    new Quaternionf().setEulerAnglesXYZ(0.0f,
                            -(float) Math.toRadians(camera.getRotation().y), 0.0f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            camera.position = camera.getPosition().add(new Vector4f(dt * cameraSpeed, 0f, 0f, FOV).rotate(
                    new Quaternionf().setEulerAnglesXYZ(0.0f,
                            -(float) Math.toRadians(camera.getRotation().y), 0.0f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            camera.position = camera.getPosition().add(new Vector4f(-dt * cameraSpeed, 0f, 0f, FOV).rotate(
                    new Quaternionf().setEulerAnglesXYZ(0.0f,
                            -(float) Math.toRadians(camera.getRotation().y), 0.0f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.position = camera.position.add(cameraUp);
        }
        if (keyboardListener.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) || keyboardListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            camera.position = camera.position.add(cameraDown);
        }


        float cursorPositionX = mouseListener.getxPosition();
        float cursorPositionDx = mouseListener.getDx();
        float cursorLastXPosition = (float) mouseListener.getxLastPosition();

        float cursorPositionY = mouseListener.getyPosition();
        float cursorPositionDy = mouseListener.getDy();
        float cursorLastYPosition = (float) mouseListener.getyLastPosition();
        if (keyboardListener.isKeyPressed(GLFW_KEY_Q)) {
            camera.rotation.add(new Vector3f(0, cameraSpeed, 0));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_E)) {
            camera.rotation.add(new Vector3f(0, -cameraSpeed, 0));
        }

        //camera.rotation.add(new Vector3f(-cursorPositionY, -cursorPositionX, 0));
//        if (cursorPositionX != cursorLastXPosition && cursorPositionX != 0.0f && cursorLastXPosition != 0.0f) {
//            System.out.println("camera.rotation.x " + camera.rotation.x);
//            System.out.println("cursorPositionDx " + cursorPositionDx);
//            camera.rotation.x = Math.max(Math.min(camera.rotation.x - (cursorPositionDy - window.getHeight() / 2.0f) / 4.0f, 90.0f), -90.0f);
//            //camera.rotation.add(new Vector3f(-cursorPositionDx, 0, 0));
//            // }
////            camera.rotation.x = Math.max(Math.min(camera.rotation.x - (cursorPositionY - window.getHeight() / 2.0f) / 4.0f, 90.0f), -90.0f);
//        }
        if (cursorPositionY != cursorLastYPosition && cursorPositionY != 0.0f && cursorLastYPosition != 0.0f) {
            camera.rotation.y = camera.rotation.y - (cursorPositionDx - window.getWidth() / 2.0f) / 4.0f;
        }

//        System.out.println("cursorLastXPosition X: " + cursorLastXPosition + " cursorLastYPosition:" + cursorLastYPosition);
//        new Quaternionf().setEulerAnglesXYZ(0.0f,
//                -(float) Math.toRadians(camera.getRotation().y), 0.0f)));
//        camera.rotation.add(camera.rotation, new Vector3f(-cursorPositionY, -cursorPositionX, 0));
//
//        camera.rotation.rotateX((float) Math.toRadians(cursorPositionDx));
//        camera.rotation.rotateY((float) Math.toRadians(cursorPositionDy));
//        if (cursorPositionX != cursorLastXPosition) {
//            camera.rotation.x = Math.max(Math.min(camera.rotation.x - (cursorPositionY - window.getHeight() / 2.0f) / 4.0f, 90.0f), -90.0f);
//
//        }
//        if (cursorPositionY != cursorLastYPosition) {
//            camera.rotation.y = camera.rotation.y - (cursorPositionX - window.getWidth() / 2.0f) / 4.0f;
//
//        }


    }

    @Override
    public void render() {
        world.gen();
        renderer.render(window, gameItems, camera, world);
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

package ru.mycubecraft.scene;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.renderer.Cube;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class LevelScene extends Scene {

    private boolean changingScene = false;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        camera.position.add(0, 1.5f + 20f, 0, 100f);
        camera.rotation.add(-80f, 0.0f, 0);
        renderer = new Renderer();
        world = new World(new BasicGen(1));

        System.out.println("WORLD GEND");

        if (world == null) {
            for (float y = -5.0f; y <= 0.0f; y++) {
                for (float x = -7.0f; x <= 8.0f; x++) {
                    for (float z = -7.0f; z <= 8.0f; z++) {
                        GameItem gameItem = new Cube(new Vector3f(1.0f, 1.0f, 1.0f), y < 0.0 ? "/textures/dirt.png" : "/textures/grass.png");
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
        AxisAngle4f axisAngle4f = new AxisAngle4f();
        axisAngle4f.x = 0.0f;
        axisAngle4f.y = -(float) Math.toRadians(camera.getRotation().y);
        axisAngle4f.z = 0.0f;

        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            camera.position = camera.position.add(new Vector4f(0f, 0, -dt * 3.0f, 10f).rotate(new Quaternionf().set(axisAngle4f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            camera.position = camera.position.add(new Vector4f(0f, 0, dt * 3.0f, 10f).rotate(new Quaternionf().set(axisAngle4f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            camera.position = camera.position.add(new Vector4f(dt * 3.0f, 0, 0f, 10f).rotate(new Quaternionf().set(axisAngle4f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            camera.position = camera.position.add(new Vector4f(-dt * 3.0f, 0, 0f, 10f).rotate(new Quaternionf().set(axisAngle4f)));
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.position = camera.position.add(new Vector4f(0.0f, dt * 3.0f, 0f, 10f));
        }
        if (keyboardListener.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) || keyboardListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            camera.position = camera.position.add(new Vector4f(0.0f, -dt * 3.0f, 0f, 10f));
        }

        float x = mouseListener.getxPosition();
        float y = mouseListener.getyPosition();
        //System.out.println("Mouse X: " + x + " Y:" + y);
        camera.rotation.x = Math.max(Math.min(camera.rotation.x - (x - window.getHeight() / 2.0f) / 4.0f, 90.0f), -90.0f);
        camera.rotation.y = (camera.rotation.y - (y - window.getWidth() / 2.0f) / 4.0f);
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

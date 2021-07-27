package ru.mycubecraft.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.Settings;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class LevelScene extends Scene {

    private final Vector3f cameraInc;
    private Hud hud;
    private float angleInc;
    private float lightAngle;
    private boolean firstTime;
    private boolean sceneChanged;
    private final Vector3f pointLightPos;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        renderer = new Renderer();
        camera = new Camera();
        world = new World(new BasicGen(1));
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        pointLightPos = new Vector3f(0.0f, 25.0f, 0.0f);
    }

    @Override
    public void init() {
        System.out.println("Entering To Word");
        hud = new Hud();
    }

    @Override
    public void update(float delta) {
        hud.rotateCompass(camera.getRotation().y);

        Vector2f rotVec = mouseListener.getDisplVec();
        camera.moveRotation(rotVec.x * Settings.MOUSE_SENSITIVITY, rotVec.y * Settings.MOUSE_SENSITIVITY, 0);

        camera.movePosition(cameraInc.x * Settings.MOVE_SPEED, cameraInc.y * Settings.MOVE_SPEED, cameraInc.z * Settings.MOVE_SPEED);

    }

    @Override
    public void input() {
        cameraInc.set(0, 0, 0);
        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_Z) || keyboardListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraInc.y = -1;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_X) || keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = 1;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_LEFT)) {
            angleInc -= 0.05f;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_RIGHT)) {
            angleInc += 0.05f;
        } else {
            angleInc = 0;
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_UP)) {
            pointLightPos.y += 0.5f;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_DOWN)) {
            pointLightPos.y -= 0.5f;
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

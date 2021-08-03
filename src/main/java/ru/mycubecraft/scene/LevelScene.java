package ru.mycubecraft.scene;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.listener.MouseInput;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

@Getter
public class LevelScene extends Scene {

    private final Vector3f cameraInc;
    private Hud hud;
    private float angleInc;
    private float lightAngle;
    private boolean firstTime;
    private boolean sceneChanged;
    private final MouseInput mouseInput;
    private Vector3f ambientLight;
    private PointLight pointLight;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        renderer = new Renderer();
        camera = new Camera();
        world = new World(new BasicGen(1));
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        mouseInput = new MouseInput();
    }

    @Override
    public void init() {
        System.out.println("Entering To Word");
        ambientLight = new Vector3f(0.1f, 0.1f, 0.1f);
        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 20.0f, 1.0f);
        float lightIntensity = 100.0f;
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);

        mouseInput.init();
        hud = new Hud();

    }

    @Override
    public void update(float delta) {
        hud.rotateCompass(camera.getRotation().y);
        pointLight.setPosition(new Vector3f(camera.getPosition().x + 1.5f, camera.getPosition().y, camera.getPosition().z + 1.5f));
        Vector2f rotVec = mouseInput.getDisplVec();
        camera.moveRotation(rotVec.x * Settings.MOUSE_SENSITIVITY, rotVec.y * Settings.MOUSE_SENSITIVITY, 0);

        camera.movePosition(cameraInc.x * Settings.MOVE_SPEED, cameraInc.y * Settings.MOVE_SPEED, cameraInc.z * Settings.MOVE_SPEED);

        int xPosition = (int) camera.getPosition().x;
        int zPosition = (int) camera.getPosition().z;
        world.generate(xPosition, zPosition);
    }

    @Override
    public void input() {
        mouseInput.input();
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

        float lightPos = pointLight.getPosition().z;
        if (keyboardListener.isKeyPressed(GLFW_KEY_N)) {
            System.out.println("\nCurrent light position: " + lightPos);
            this.pointLight.getPosition().z = lightPos + 2.1f;
            System.out.println("Moved light position: " + this.pointLight.getPosition().z);
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_M)) {
            System.out.println("\nCurrent light position: " + lightPos);
            this.pointLight.getPosition().z = lightPos - 2.1f;
            System.out.println("Moved light position: " + this.pointLight.getPosition().z);
        }
    }


    @Override
    public void render(float delta) {
        renderer.render(window, gameItems, world, camera, skyBox, this, hud, ambientLight, pointLight);
        hud.updateFps(delta);
        int filteredBlocksCount = renderer.getFilteredItems().size();
        hud.updateHud(window, camera, world, filteredBlocksCount);
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

package ru.mycubecraft.scene;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.listener.MouseInput;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.window.Window;
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
    private DirectionalLight directionalLight;

    private boolean dayCycle = false;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        renderer = new Renderer();
        camera = new Camera();
        world = new World(new BasicGen(1));
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        mouseInput = new MouseInput();
        lightAngle = -90;
    }

    @Override
    public void init() {
        System.out.println("Entering To Word");

        ambientLight = new Vector3f(0.79f, 0.91f, 0.96f);
        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 10.0f, 1);
        float lightIntensity = 10.0f;
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);

        lightPosition = new Vector3f(-1, 0, 0);
        lightColour = new Vector3f(1, 1, 1);
        directionalLight = new DirectionalLight(lightColour, lightPosition, lightIntensity);

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

        // Update directional light direction, intensity and colour
        lightAngle += 0.01f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);

        int xPosition = (int) camera.getPosition().x;
        int zPosition = (int) camera.getPosition().z;
        world.generate(xPosition, zPosition);
    }

    private void lightUpdate() {
        float stage = 0.01f;
        if (dayCycle) {
            ambientLight.sub(new Vector3f(stage, stage,stage));
            Window.red -= stage;
            Window.green -= stage;
            Window.blue -= stage;
        } else {
            ambientLight.add(new Vector3f(stage, stage, stage));
            if (Window.red < 0.79f && Window.green < 0.91f  && Window.blue < 0.96f ) {
                Window.red += stage;
                Window.green += stage;
                Window.blue += stage;
            }
        }
        if (ambientLight.x >= 0.79f || ambientLight.y >= 0.91f  || ambientLight.z >= 0.96f ) {
            dayCycle = true;
        } else if (ambientLight.x <= 0 || ambientLight.y <= 0  || ambientLight.z <= 0 ) {
            dayCycle = false;
        }

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
        renderer.render(window, gameItems, world, camera, skyBox, this, hud, ambientLight, pointLight, directionalLight);
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

package ru.mycubecraft.scene;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.engine.graph.SpotLight;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.listener.MouseInput;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.MouseBoxSelectionDetector;
import ru.mycubecraft.world.World;

import static org.lwjgl.glfw.GLFW.*;

@Getter
public class LevelScene extends Scene {

    private final Vector3f cameraInc;
    private final MouseInput mouseInput;
    private final boolean dayCycle = false;
    private Hud hud;
    private boolean firstTime;
    private boolean sceneChanged;
    private Vector3f ambientLight;
    private PointLight[] pointLightList;
    private SpotLight[] spotLightList;
    private DirectionalLight directionalLight;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;
    private boolean leftButtonPressed = false;
    private Vector3f selectedItemPosition;

    public LevelScene() {
        System.err.println("Entered to a Level Scene");
        renderer = new Renderer();
        camera = new Camera();
        world = new World(new BasicGen(1));
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        mouseInput = new MouseInput();
        lightAngle = -90;
        fog = Fog.NOFOG;
    }

    @Override
    public void init() {
        mouseBoxSelectionDetector = new MouseBoxSelectionDetector();
//        sun.getGameCubeItem().setScale(3f);
//        sun.getGameCubeItem().setPosition(-3000, 0.0f, 0F);
        ambientLight = new Vector3f(1f, 1f, 1f);

        // Point Light
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 0.8f;
        PointLight pointLight = new PointLight(new Vector3f(1f, 1f, 1f), lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        pointLightList = new PointLight[]{pointLight};

        // Spot Light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        pointLight = new PointLight(new Vector3f(1f, 1f, 1f), lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.01f);
        pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        spotLightList = new SpotLight[]{spotLight, new SpotLight(spotLight)};

        lightPosition = new Vector3f(-1, 0, 0);
        directionalLight = new DirectionalLight(new Vector3f(1f, 1f, 1f), lightPosition, lightIntensity);

        mouseInput.init();
        hud = new Hud();

        // Fog
        Vector3f fogColour = new Vector3f(0.49f, 0.61f, 0.66f);

        if (Settings.SHOW_FOG) {
            this.fog = new Fog(true, fogColour, 0.04f);
        }

    }

    @Override
    public void update(float delta) {
        //hudUpdate(delta);
        mouseBoxSelectionDetector.update(camera);

        Vector2f rotVec = mouseInput.getDisplVec();
        camera.moveRotation(rotVec.x * Settings.MOUSE_SENSITIVITY, rotVec.y * Settings.MOUSE_SENSITIVITY, 0);
        camera.movePosition(cameraInc.x * Settings.MOVE_SPEED, cameraInc.y * Settings.MOVE_SPEED, cameraInc.z * Settings.MOVE_SPEED);

        lightUpdate(delta);
        //gameItems.add(sun.getGameCubeItem());
        int xPosition = (int) camera.getPosition().x;
        int yPosition = (int) camera.getPosition().y;
        int zPosition = (int) camera.getPosition().z;
        world.generate(xPosition, yPosition, zPosition);
        selectedItemPosition = mouseBoxSelectionDetector.getGameItemPosition(world.getChunksBlockItems(), camera);
    }

    private void lightUpdate(float delta) {
        // Update spot light direction
        spotAngle += spotInc * 0.05f;
        if (spotAngle > 2) {
            spotInc = -1;
        } else if (spotAngle < -2) {
            spotInc = 1;
        }
//        double spotAngleRad = Math.toRadians(spotAngle);
//        Vector3f coneDir = spotLightList[0].getConeDirection();
//        coneDir.y = (float) Math.sin(spotAngleRad);
        // Update directional light direction, intensity and colour
        lightAngle += 0.5f;
        //this.sun.getGameCubeItem().setPosition(directionalLight.getDirection().x * 1000f, directionalLight.getDirection().y * 1000f, directionalLight.getDirection().z * 1000f);
        //if (lightAngle > 90) {
        //directionalLight.setIntensity(1);
        if (lightAngle >= 360) {
            lightAngle = -90;
        }
        //ambientLight.set(0.8f, 0.8f, 0.8f);
        //  } else if (lightAngle <= -80 || lightAngle >= 80) {
//            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
//            ambientLight.set(Math.min(factor, 0.79f), Math.min(factor, 0.91f), Math.min(factor, 0.96f));
//            directionalLight.setIntensity(Math.min(factor, 0.91f));
//            directionalLight.getColor().y = Math.min(factor, 0.9f);
//            directionalLight.getColor().z = Math.min(factor, 0.5f);
//        } else {
//            ambientLight.set(0.8f, 0.8f, 0.8f);
//            directionalLight.setIntensity(0.91f);
//            directionalLight.getColor().x = 0.79f;
//            directionalLight.getColor().y = 0.91f;
//            directionalLight.getColor().z = 0.96f;
//        }

//        double angRad = Math.toRadians(lightAngle);
//        directionalLight.getDirection().x = (float) Math.sin(angRad);
//        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    private void hudUpdate(float delta) {
        hud.rotateCompass(-camera.getRotation().y);
        int filteredBlocksCount = renderer.getFilteredItems().size();
        hud.updateHud(window, camera, world, filteredBlocksCount);
        hud.updateFps(delta);
        if (selectedItemPosition != null) {
            hud.updateTargetObjectInfo(selectedItemPosition);
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
        boolean aux = mouseInput.isLeftButtonPressed();
        if (aux && !this.leftButtonPressed) {
            if (selectedItemPosition != null) {
                createGameBlockItem(selectedItemPosition);
            }
        }
        this.leftButtonPressed = aux;
        float lightPos = spotLightList[0].getPointLight().getPosition().z;
        if (keyboardListener.isKeyPressed(GLFW_KEY_N)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_M)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
        }
    }


    @Override
    public void render(float delta) {
        renderer.render(window, gameItems, world, camera, skyBox, this, hud, ambientLight, pointLightList, spotLightList, directionalLight);
        hudUpdate(delta);
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

    private void createGameBlockItem(Vector3f position) {
        Vector3f newBlockPosition = new Vector3f(position);
        Vector3f ray = mouseBoxSelectionDetector.rayDirection().negate();
        float xStart = (float) Math.ceil(ray.x);
        float yStart = (float) Math.ceil(ray.y);
        float zStart = (float) Math.ceil(ray.z);
        newBlockPosition.add(xStart, yStart, zStart);

        boolean containsChunk = world.containsChunk(newBlockPosition);
        Chunk chunk;
        if (!containsChunk) {
            chunk = world.addChunk(newBlockPosition);
            chunk.addBlock(newBlockPosition);
        } else {
            chunk = world.getChunk(newBlockPosition);
            boolean chunkContainsBlock = chunk.containsBlock(newBlockPosition);
            if (chunkContainsBlock) {
                xStart = (float) Math.ceil(ray.x);
                yStart = (float) Math.ceil(ray.y);
                zStart = (float) Math.ceil(ray.z);
                newBlockPosition.add(xStart, yStart, zStart);
            }
            chunk.addBlock(newBlockPosition);
        }
    }
}

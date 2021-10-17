package ru.mycubecraft.scene;

import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Contact;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.engine.Timer;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.MouseBoxSelectionDetector;
import ru.mycubecraft.world.World;
import ru.mycubecraft.world.player.Player;
import ru.mycubecraft.world.player.impl.DefaultPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static ru.mycubecraft.Game.caps;

@Getter
public class LevelScene extends Scene {

    private final Vector3f playerVelocity = new Vector3f(0.0f, 0.0f, 0.0f);
    private final Vector3f playerAcceleration = new Vector3f(0f, -4f, 0f);
    private final Vector3f tmpv3f = new Vector3f();

    private final Vector3f cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);

    /**
     * Used for timing calculations.
     */
    private final Timer timer;
    private final Player player;
    private Hud hud;
    private Vector3f ambientLight;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;
    private boolean leftButtonPressed = false;
    private Vector3f selectedItemPosition;

    public LevelScene() {
        timer = new Timer();
        System.err.println("Entered to a Level Scene");
        camera = new Camera();
        world = new World();
        lightAngle = -90;
        fog = Fog.NOFOG;
        player = new DefaultPlayer();
    }

    /**
     * Run the "update and render" loop in a separate thread.
     * <p>
     * This is to decouple rendering from possibly long-blocking polling of OS/window messages (via
     * {@link org.lwjgl.glfw.GLFW#glfwPollEvents()}).
     */
    @Override
    public void runUpdateAndRenderLoop() {
        long window = Window.getInstance().getGlfwWindow();
        glfwMakeContextCurrent(window);
        GL.setCapabilities(caps);
        AssetPool.loadAssets();
        timer.init();
        renderer = new Renderer();
        renderer.init();
        world.generateStartChunks();
        /* Initialize timer */
        timer.init();
        mouseBoxSelectionDetector = new MouseBoxSelectionDetector();
//        sun.getGameCubeItem().setScale(3f);
//        sun.getGameCubeItem().setPosition(-3000, 0.0f, 0F);
        ambientLight = new Vector3f(1f, 1f, 1f);

        hud = new Hud();
        hud.buildHud();

        // Fog
        Vector3f fogColour = new Vector3f(0.49f, 0.61f, 0.66f);

        if (Settings.SHOW_FOG) {
            this.fog = new Fog(true, fogColour, 0.04f);
        }

        float delta;
        while (!glfwWindowShouldClose(window)) {

            /* Get delta time */
            delta = timer.getDelta();
            System.out.print("\rCurrent DELTA: " + delta);
            input();

            /* Update game and timer UPS */
            update(delta);
            timer.updateUPS();

            /*
             * Execute any runnables that have accumulated in the render queue.
             * These are GL calls for created/updated chunks.
             */
            drainRunnables();

            /* Render game and update timer FPS */
            render();
            timer.updateFPS();

            /* Update timer */
            timer.update();

            int currentFps = timer.getFPS();
            int currentUps = timer.getUPS();
            hudUpdate(currentFps, currentUps, delta);
            System.out.print("\rCurrent FPS: " + currentFps);

            glfwSwapBuffers(window);
        }
        cleanup();
        drainRunnables();
        GL.setCapabilities(null);
    }

    @Override
    public void update(float delta) {
        lightUpdate();

        mouseBoxSelectionDetector.update(camera);

        float dangx = mouseListener.getDangx();
        float dangy = mouseListener.getDangy();
        float angx = mouseListener.getAngx();
        float angy = mouseListener.getAngy();

        angx += dangx * Settings.MOUSE_SENSITIVITY;
        angy += dangy * Settings.MOUSE_SENSITIVITY;
        dangx *= 0.0994f;
        dangy *= 0.0994f;
        mouseListener.setDangx(dangx);
        mouseListener.setDangy(dangy);

        if (!player.isFly()) {
            cameraInc.add(playerAcceleration);
            handleCollisions(delta * 10, cameraInc, camera.getPosition());
        } else {
            cameraInc.mul(delta).add(playerAcceleration);
        }
        camera.moveRotation(angx, angy, 0);
        camera.movePosition(cameraInc.x * delta * Settings.MOVE_SPEED, cameraInc.y * delta * Settings.MOVE_SPEED, cameraInc.z * delta * Settings.MOVE_SPEED);

        int xPosition = (int) camera.getPosition().x;
        int zPosition = (int) camera.getPosition().z;
        world.ensureChunk(xPosition, zPosition);

        //selectedItemPosition = mouseBoxSelectionDetector.getGameItemPosition(world.getChunksBlockItems(), camera);
    }

    private void lightUpdate() {
        // Update spot light direction
        spotAngle += spotInc * 0.15f;
        if (spotAngle > 2) {
            spotInc = -1;
        } else if (spotAngle < -2) {
            spotInc = 1;
        }

        lightAngle += 0.5f;
        if (lightAngle >= 360) {
            lightAngle = -90;
        }
    }

    private void hudUpdate(int fps, int ups, float delta) {
        hud.rotateCompass(-camera.getRotation().y);
        int filteredBlocksCount = renderer.getFilteredItems().size();
        hud.updateHud(camera, world, filteredBlocksCount);
        hud.updateFps(fps);
        hud.updateUps(ups);
        hud.updateDelta(delta);
        if (selectedItemPosition != null) {
            hud.updateTargetObjectInfo(selectedItemPosition);
        }
    }

    @Override
    public void input() {
        cameraInc.set(0, 0, 0);

        boolean fly = player.isFly();
        boolean jumping = player.isJumping();

        float factor = fly ? 1f : 2f;
        if (keyboardListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraInc.y = -factor;
        } else if (fly && keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = factor;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -factor;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = factor;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -factor;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = factor;
        }

        if (!fly && keyboardListener.isKeyPressed(GLFW_KEY_SPACE) && !jumping) {
            cameraInc.y = factor * 2 * 2;
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_F)) {
            player.setFly(!fly);
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_R)) {
            camera.setPosition(92.0f, 110f, 29.0f);
        }

        boolean aux = false;
        if (aux && !this.leftButtonPressed) {
            if (selectedItemPosition != null) {
                createGameBlockItem(selectedItemPosition);
            }
        }
        this.leftButtonPressed = aux;
    }


    @Override
    public void render() {
        renderer.render(world, camera, this, hud, ambientLight);
    }

    @Override
    public void cleanup() {
        hud.cleanup();
        renderer.cleanup();
        world.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.cleanup();
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
            //chunk.addBlock(newBlockPosition);
        } else {
            chunk = world.getChunk(newBlockPosition);
//            //boolean chunkContainsBlock = chunk.containsBlock(newBlockPosition);
//            if (chunkContainsBlock) {
//                xStart = (float) Math.ceil(ray.x);
//                yStart = (float) Math.ceil(ray.y);
//                zStart = (float) Math.ceil(ray.z);
//                newBlockPosition.add(xStart, yStart, zStart);
//            }
            //chunk.addBlock(newBlockPosition);
        }
    }


    /**
     * Handle any collisions with the player and the blocks.
     */
    private void handleCollisions(float dt, Vector3f velocity, Vector4f position) {
        List<Contact> contacts = new ArrayList<>();
        world.collisionDetection(dt, velocity, position, contacts);
        world.collisionResponse(dt, velocity, position, contacts);
    }


}

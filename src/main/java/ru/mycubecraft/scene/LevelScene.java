package ru.mycubecraft.scene;

import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import ru.mycubecraft.DelayedRunnable;
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

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;

    private final Vector3f playerVelocity = new Vector3f(0.0f, 0.0f, 0.0f);
    private final Vector3f playerAcceleration = new Vector3f(0, -2.0f, 0f);
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
        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
            renderer.init();
            world.generateStartChunks();
            return null;
        }, "Shaders creation initialize", 0));
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

        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;
        long current = System.currentTimeMillis();

        while (!glfwWindowShouldClose(window)) {

            /* Get delta time and update the accumulator */
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();
            /* Update game and timer UPS if enough time has passed */
            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            float currentFps = 1000f / (-current + (current = System.currentTimeMillis()));
            hudUpdate(currentFps);
            System.out.print("\rWithout UPS FPS: " + currentFps);
            /*
             * Execute any runnables that have accumulated in the render queue.
             * These are GL calls for created/updated chunks.
             */
            drainRunnables();

            render();
            glfwSwapBuffers(window);
        }
        cleanup();
        drainRunnables();
        GL.setCapabilities(null);
    }

    @Override
    public void update(float delta) {
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
            handleCollisions(delta * 5f, cameraInc, camera.getPosition());
        } else {
            cameraInc.add(playerVelocity.x, playerVelocity.y, playerVelocity.z);
        }
        camera.moveRotation(angx, angy, 0);
        camera.movePosition(cameraInc.x * Settings.MOVE_SPEED, cameraInc.y * Settings.MOVE_SPEED, cameraInc.z * Settings.MOVE_SPEED);

        lightUpdate();
        int xPosition = (int) camera.getPosition().x;
        int zPosition = (int) camera.getPosition().z;

        world.ensureChunk(xPosition, zPosition);

        //selectedItemPosition = mouseBoxSelectionDetector.getGameItemPosition(world.getChunksBlockItems(), camera);
    }

    private void lightUpdate() {
        // Update spot light direction
        spotAngle += spotInc * 0.05f;
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

    private void hudUpdate(float delta) {
        hud.rotateCompass(-camera.getRotation().y);
        int filteredBlocksCount = renderer.getFilteredItems().size();
        hud.updateHud(camera, world, filteredBlocksCount);
        hud.updateFps(delta);
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
                cameraInc.add(0, 5, 0);
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
     * Handle any collisions with the player and the voxels.
     */
    private void handleCollisions(float dt, Vector3f velocity, Vector4f position) {
        List<Contact> contacts = new ArrayList<>();
        world.collisionDetection(dt, velocity, position, contacts);
        world.collisionResponse(dt, velocity, position, contacts);
    }


}

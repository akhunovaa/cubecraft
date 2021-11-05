package ru.mycubecraft.scene;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;
import org.lwjgl.opengl.GL;
import ru.mycubecraft.DelayedRunnable;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.data.Contact;
import ru.mycubecraft.data.Hud;
import ru.mycubecraft.engine.Timer;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.engine.sound.SoundBuffer;
import ru.mycubecraft.engine.sound.SoundListener;
import ru.mycubecraft.engine.sound.SoundManager;
import ru.mycubecraft.engine.sound.SoundSource;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.World;
import ru.mycubecraft.world.player.Player;
import ru.mycubecraft.world.player.impl.DefaultPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static ru.mycubecraft.Game.caps;

@Slf4j
@Getter
public class LevelScene extends Scene {

    private final Vector3f playerVelocity = new Vector3f(0.0f, 0.0f, 0.0f);
    private final Vector3f playerAcceleration = new Vector3f(0f, -4f, 0f);
    private final Vector3f tmpv3f = new Vector3f();

    /**
     * Used for timing calculations.
     */
    private final Timer timer;
    private Hud hud;
    private Vector3f ambientLight;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;
    private boolean colourClearPButtonPressed;

    public LevelScene() {
        timer = new Timer();
        log.info("Entered to a Level Scene");
        world = new World();
        lightAngle = -90;
        player = new DefaultPlayer();
        soundMgr = new SoundManager();
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
        if (Settings.MUSIC_PLAY) {
            this.soundMgr.init();
            this.soundMgr.setAttenuationModel(AL11.AL_SPEED_OF_SOUND);
            try {
                setupSounds();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        renderer.init();
        world.generateStartChunks();
        /* Initialize timer */
        timer.init();

//        sun.getGameCubeItem().setScale(3f);
//        sun.getGameCubeItem().setPosition(-3000, 0.0f, 0F);
        ambientLight = new Vector3f(0.8f, 0.8f, 0.8f);

        hud = new Hud();
        hud.buildHud();

        // Fog
        Vector3f fogColour = new Vector3f(0.49f, 0.61f, 0.66f);

        this.fog = this.fogLButtonPressed ? new Fog(true, fogColour, 0.08f) : Fog.NOFOG;


        float delta;
        while (!glfwWindowShouldClose(window)) {
            boolean isColorButtonPressed = this.colourClearPButtonPressed;

            if (isColorButtonPressed) {
                glClearColor(0.49f, 0.61f, 0.66f, 1f);
            } else {
                glClearColor(0f, 0f, 0f, 0f);
            }

            /* Get delta time */
            delta = timer.getDelta();
//            System.out.print("\nCurrent DELTA: " + delta);
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

            glfwSwapBuffers(window);
        }
        cleanup();
        drainRunnables();
        GL.setCapabilities(null);
    }

    private void setupSounds() throws Exception {
        SoundBuffer buffBack = new SoundBuffer("assets/sounds/pigeonych-8bit.ogg");
        soundMgr.addSoundBuffer(buffBack);
        SoundSource sourceBack = new SoundSource(true, true);
        sourceBack.setBuffer(buffBack.getBufferId());
        soundMgr.addSoundSource(Sounds.MUSIC.toString(), sourceBack);
        soundMgr.setListener(new SoundListener(new Vector3f()));
        sourceBack.play();
    }

    @Override
    public void update(float delta) {

        int xPosition = (int) player.getPosition().x;
        int zPosition = (int) player.getPosition().z;

        world.ensureChunkIfVisible(xPosition, zPosition);

        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
            world.destroyOutOfRenderDistanceFrontierChunks(xPosition, zPosition);
            return null;
        }, "Worlds chunk destroyer!", 0));

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

        float fixedDelta = delta * Settings.MOVE_SPEED;
        if (!player.isFly()) {
            playerVelocity.add(playerAcceleration);
            handleCollisions(fixedDelta, playerVelocity, player);
        }

        player.moveRotation(angx, angy, 0);

        player.movePosition(playerVelocity.x * fixedDelta, playerVelocity.y * fixedDelta, playerVelocity.z * fixedDelta);

        Chunk chunk = world.getChunk(xPosition, zPosition);
        // Determine the selected block in the center of the viewport.
        player.findAndSelectBlock(chunk.getBlockField());
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
        hud.rotateCompass(-player.getRotation().y);
        int filteredBlocksCount = renderer.getFilteredItems().size();
        hud.updateHud(player, world, filteredBlocksCount);
        hud.updateFps(fps);
        hud.updateUps(ups);
        hud.updateDelta(delta);
        if (player.getSelectedBlock() != null) {
            hud.updateTargetObjectInfo(player.getSelectedBlock().getPosition());
        }
    }

    @Override
    public void input() {
        playerVelocity.set(0, 0, 0);

        boolean fly = player.isFly();
        boolean jumping = player.isJumping();

        float factor = fly ? 6f : 2f;
        if (keyboardListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            playerVelocity.y = -factor;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            playerVelocity.y = factor;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_W)) {
            playerVelocity.z = -factor;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_S)) {
            playerVelocity.z = factor;
        }
        if (keyboardListener.isKeyPressed(GLFW_KEY_A)) {
            playerVelocity.x = -factor;
        } else if (keyboardListener.isKeyPressed(GLFW_KEY_D)) {
            playerVelocity.x = factor;
        }

        if (!fly && keyboardListener.isKeyPressed(GLFW_KEY_SPACE) && !jumping) {
            playerVelocity.y = factor * 2 * 2;
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_F)) {
            player.setFly(!fly);
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_R)) {
            player.setPosition(47.0f, 90f, -179f);
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_P)) {
            this.colourClearPButtonPressed = !this.colourClearPButtonPressed;
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_L)) {
            this.fogLButtonPressed = !this.fogLButtonPressed;
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_B)) {
            this.soundMgr.playSoundSource(Sounds.MUSIC.toString());
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_N)) {
            this.soundMgr.stopSoundSource(Sounds.MUSIC.toString());
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_M)) {
            this.soundMgr.pauseSoundSource(Sounds.MUSIC.toString());
        }

        if (mouseListener.isLeftButtonPressed()) {
            int xPosition = (int) player.getPosition().x;
            int zPosition = (int) player.getPosition().z;

            Chunk chunk = world.getChunk(xPosition, zPosition);
            player.placeAtSelectedBlock(chunk.getBlockField());
            chunk.sortBlocksVisibility();
            mouseListener.setLeftButtonPressed(false);
        }

        if (mouseListener.isRightButtonPressed()) {
            int xPosition = (int) player.getPosition().x;
            int zPosition = (int) player.getPosition().z;

            Chunk chunk = world.getChunk(xPosition, zPosition);
            player.removeSelectedBlock(chunk.getBlockField());
            chunk.sortBlocksVisibility();
            mouseListener.setRightButtonPressed(false);
        }
    }

    @Override
    public void render() {
        renderer.render(world, player, this, hud, ambientLight);
    }

    /**
     * Handle any collisions with the player and the blocks.
     */
    private void handleCollisions(float dt, Vector3f velocity, Player player) {
        List<Contact> contacts = new ArrayList<>();
        world.collisionDetection(dt, velocity, player, contacts);
        world.collisionResponse(dt, velocity, player, contacts);
    }

    @Override
    public void cleanup() {
        hud.cleanup();
        renderer.cleanup();
        soundMgr.cleanup();
        world.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.cleanup();
            gameItem.getMesh().cleanUp();
        }
    }

    private enum Sounds {MUSIC, BEEP, FIRE}


}

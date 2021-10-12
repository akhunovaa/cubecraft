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
import ru.mycubecraft.world.BasicGen;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.MouseBoxSelectionDetector;
import ru.mycubecraft.world.World;
import ru.mycubecraft.world.player.Player;
import ru.mycubecraft.world.player.impl.DefaultPlayer;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.*;
import static java.util.Collections.sort;
import static org.lwjgl.glfw.GLFW.*;
import static ru.mycubecraft.Game.caps;

@Getter
public class LevelScene extends Scene {

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;
    /**
     * The height of a chunk (in number of voxels).
     */
    private static final int CHUNK_HEIGHT = 64;
    private final Vector3f cameraInc;
    private final boolean dayCycle = false;
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
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
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

        camera.moveRotation(angx, angy, 0);
        camera.movePosition(cameraInc.x * Settings.MOVE_SPEED, cameraInc.y * Settings.MOVE_SPEED, cameraInc.z * Settings.MOVE_SPEED);
        if (!player.isFly()) {
            // System.out.println("\n");
            //System.out.println("cameraInc X: " + cameraInc.x + " Y: " + cameraInc.y + " Z: " + cameraInc.z);
            //handleCollisions(delta, cameraInc, camera.getPosition());
        }
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

        boolean fly = !player.isFly();
        boolean jumping = player.isJumping();

        float factor = fly ? 2f : 1f;
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
            player.setJumping(true);
            cameraInc.add(0, 13, 0);
        } else if (!keyboardListener.isKeyPressed(GLFW_KEY_SPACE)) {
            player.setJumping(false);
        }

        if (keyboardListener.isKeyPressed(GLFW_KEY_F)) {
            player.setFly(!fly);
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
        collisionDetection(dt, velocity, position, contacts);
        System.out.println("\n");
        System.out.println("contacts: " + contacts.size());
        collisionResponse(dt, velocity, position, contacts);
    }

    /**
     * Detect possible collision candidates.
     */
    private void collisionDetection(float dt, Vector3f velocity, Vector4f position, List<Contact> contacts) {
        float dx = velocity.x * dt,
                dy = velocity.y * dt,
                dz = velocity.z * dt;
        int minX = (int) floor(position.x - Player.PLAYER_WIDTH + (dx < 0 ? dx : 0));
        int maxX = (int) floor(position.x + Player.PLAYER_WIDTH + (dx > 0 ? dx : 0));
        int minY = (int) floor(position.y - Player.PLAYER_EYE_HEIGHT + (dy < 0 ? dy : 0));
        int maxY = (int) floor(position.y + Player.PLAYER_HEIGHT - Player.PLAYER_EYE_HEIGHT + (dy > 0 ? dy : 0));
        int minZ = (int) floor(position.z - Player.PLAYER_WIDTH + (dz < 0 ? dz : 0));
        int maxZ = (int) floor(position.z + Player.PLAYER_WIDTH + (dz > 0 ? dz : 0));
        /* Just loop over all voxels that could possibly collide with the player */
        for (int y = min(CHUNK_HEIGHT - 1, maxY); y >= 0 && y >= minY; y--) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
//                    if (load(x, y, z) == EMPTY_VOXEL)
//                        continue;
                    /* and perform swept-aabb intersection */
                    //intersectSweptAabbAabb(x, y, z, position.x - x, position.y - y, position.z - z, dx, dy, dz, contacts);
                }
            }
        }
    }

    /**
     * Respond to all found collision contacts.
     */
    private void collisionResponse(float dt, Vector3f v, Vector4f p, List<Contact> contacts) {
        sort(contacts);
        int minX = Integer.MIN_VALUE, maxX = Integer.MAX_VALUE, minY = Integer.MIN_VALUE, maxY = Integer.MAX_VALUE, minZ = Integer.MIN_VALUE,
                maxZ = Integer.MAX_VALUE;
        float elapsedTime = 0f;
        float dx = v.x * dt, dy = v.y * dt, dz = v.z * dt;
        for (Contact contact : contacts) {
            if (contact.x <= minX || contact.y <= minY || contact.z <= minZ || contact.x >= maxX || contact.y >= maxY || contact.z >= maxZ)
                continue;
            float t = contact.t - elapsedTime;
            p.add(dx * t, dy * t, dz * t, 0);
            elapsedTime += t;
            if (contact.nx != 0) {
                minX = dx < 0 ? max(minX, contact.x) : minX;
                maxX = dx < 0 ? maxX : min(maxX, contact.x);
                v.x = 0f;
                dx = 0f;
            } else if (contact.ny != 0) {
                minY = dy < 0 ? max(minY, contact.y) : contact.y - (int) Player.PLAYER_HEIGHT;
                maxY = dy < 0 ? contact.y + (int) ceil(Player.PLAYER_HEIGHT) + 1 : min(maxY, contact.y);
                v.y = 0f;
                dy = 0f;
            } else if (contact.nz != 0) {
                minZ = dz < 0 ? max(minZ, contact.z) : minZ;
                maxZ = dz < 0 ? maxZ : min(maxZ, contact.z);
                v.z = 0f;
                dz = 0f;
            }
        }
        float trem = 1f - elapsedTime;
        p.add(dx * trem, dy * trem, dz * trem, 0);
    }

    /**
     * Compute the exact collision point between the player and the voxel at <code>(x, y, z)</code>.
     */
    private void intersectSweptAabbAabb(int x, int y, int z, float px, float py, float pz, float dx, float dy, float dz, List<Contact> contacts) {
        /*
         * https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-
         * collision-detection-and-response-r3084/
         */
        float pxmax = px + Player.PLAYER_WIDTH,
                pxmin = px - Player.PLAYER_WIDTH,
                pymax = py + Player.PLAYER_HEIGHT - Player.PLAYER_EYE_HEIGHT,
                pymin = py - Player.PLAYER_EYE_HEIGHT,
                pzmax = pz + Player.PLAYER_WIDTH,
                pzmin = pz - Player.PLAYER_WIDTH;

        float xInvEntry = dx > 0f ? -pxmax : 1 - pxmin,
                xInvExit = dx > 0f ? 1 - pxmin : -pxmax;

        boolean xNotValid = dx == 0;

        float xEntry = xNotValid ? NEGATIVE_INFINITY : xInvEntry / dx,
                xExit = xNotValid ? POSITIVE_INFINITY : xInvExit / dx;

        float yInvEntry = dy > 0f ? -pymax : 1 - pymin,
                yInvExit = dy > 0f ? 1 - pymin : -pymax;

        boolean yNotValid = dy == 0;

        float yEntry = yNotValid ? NEGATIVE_INFINITY : yInvEntry / dy,
                yExit = yNotValid ? POSITIVE_INFINITY : yInvExit / dy;

        float zInvEntry = dz > 0f ? -pzmax : 1 - pzmin,
                zInvExit = dz > 0f ? 1 - pzmin : -pzmax;

        boolean zNotValid = dz == 0;

        float zEntry = zNotValid ? NEGATIVE_INFINITY : zInvEntry / dz,
                zExit = zNotValid ? POSITIVE_INFINITY : zInvExit / dz;

        float tEntry = max(max(xEntry, yEntry), zEntry),
                tExit = min(min(xExit, yExit), zExit);

        if (tEntry < -.5f || tEntry > tExit) {
            return;
        }

        Contact c;
        contacts.add(c = new Contact(tEntry, x, y, z));

        if (xEntry == tEntry) {
            c.nx = dx > 0 ? -1 : 1;
        } else if (yEntry == tEntry) {
            c.ny = dy > 0 ? -1 : 1;
        } else {
            c.nz = dz > 0 ? -1 : 1;
        }
    }
}

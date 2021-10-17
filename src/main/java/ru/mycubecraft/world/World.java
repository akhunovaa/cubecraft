package ru.mycubecraft.world;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.data.Contact;
import ru.mycubecraft.engine.Utils;
import ru.mycubecraft.world.player.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.*;
import static java.util.Collections.sort;
import static ru.mycubecraft.world.player.Player.*;

@Slf4j
@Getter
public class World {

    // 256 chunks & 4096 blocks in one chunk & totally 1 048 576 blocks
    public static final int WORLD_WIDTH = 2;
    public static final int WORLD_HEIGHT = 8;
    public static final int WORLD_SIZE = 5 * 8; // 4=64 5=100 6=144
    /**
     * The height of a chunk (in number of block).
     */
    private static final int CHUNK_HEIGHT = 256;
    /**
     * The width and depth of a chunk (in number of blocks).
     */
    private static final int CHUNK_SIZE_SHIFT = 5;
    private static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;

    /**
     * Used to offload compute-heavy tasks, such as chunk meshing and triangulation, from the render
     * thread to background threads.
     */
    protected final ExecutorService executorService = Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() / 2), r -> {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("Data builder");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, Chunk> chunkMap = Utils.createLRUMap(12);

    public World() {
    }

    public void ensureChunk(int xPosition, int zPosition) {
        int cx = xPosition >> CHUNK_SIZE_SHIFT,
                cz = zPosition >> CHUNK_SIZE_SHIFT;
        generateChunk(cx, cz);
    }

    public void generateStartChunks() {
        generateChunk(-1, -1);
        generateChunk(1, 1);
    }

    private void generateChunk(int cx, int cz) {
        for (int x = (cx); x < (cx + 2); x++) {
            for (int z = (cz); z < (cz + 2); z++) {
                String chunkKey = idx(x, z);
                if (!chunkMap.containsKey(chunkKey)) {
                    Chunk chunk = createChunk(x, z);
                    chunkMap.put(chunkKey, chunk);
                    executorService.submit(() -> {
                        try {
                            chunk.createBlockField();
                            chunk.sortBlocksVisibility();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    /**
     * Detect possible collision candidates.
     */
    public void collisionDetection(float dt, Vector3f velocity, Vector4f position, List<Contact> contacts) {
        int xPosition = (int) position.x;
        int zPosition = (int) position.z;

        int cx = xPosition >> CHUNK_SIZE_SHIFT,
                cz = zPosition >> CHUNK_SIZE_SHIFT;
        String chunkKey = idx(cx, cz);
        Chunk chunk = chunkMap.get(chunkKey);
        if (chunk == null || chunk.getBlockField() == null) {
            return;
        }
        BlockField blockField = chunk.getBlockField();
        float dx = velocity.x * dt,
                dy = velocity.y * dt,
                dz = velocity.z * dt;
        int minX = (int) floor(position.x - Player.PLAYER_WIDTH + (dx < 0 ? dx : 0));
        int maxX = (int) floor(position.x + Player.PLAYER_WIDTH + (dx > 0 ? dx : 0));
        int minY = (int) floor(position.y - Player.PLAYER_EYE_HEIGHT + (dy < 0 ? dy : 0));
        int maxY = (int) floor(position.y + Player.PLAYER_HEIGHT - Player.PLAYER_EYE_HEIGHT + (dy > 0 ? dy : 0));
        int minZ = (int) floor(position.z - Player.PLAYER_WIDTH + (dz < 0 ? dz : 0));
        int maxZ = (int) floor(position.z + Player.PLAYER_WIDTH + (dz > 0 ? dz : 0));
        /* Just loop over all blocks that could possibly collide with the player */
        for (int y = min(CHUNK_HEIGHT - 1, maxY); y >= 0 && y >= minY; y--) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    Block block = blockField.load(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    /* and perform swept-aabb intersection */
                    intersectSweptAabbAabb(x, y, z, position.x - x,
                            position.y - y, position.z - z, dx, dy, dz, contacts, blockField);
                }
            }
        }
    }

    /**
     * Compute the exact collision point between the player and the block at <code>(x, y, z)</code>.
     */
    private void intersectSweptAabbAabb(int x, int y, int z, float px, float py,
                                        float pz, float dx, float dy, float dz,
                                        List<Contact> contacts, BlockField blockField) {
        /*
         * https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
         */
        float pxmax = px + PLAYER_WIDTH, pxmin = px - PLAYER_WIDTH, pymax = py + PLAYER_HEIGHT - PLAYER_EYE_HEIGHT, pymin = py - PLAYER_EYE_HEIGHT,
                pzmax = pz + PLAYER_WIDTH, pzmin = pz - PLAYER_WIDTH;

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

        Contact contact = new Contact(tEntry, x, y, z);

        if (xEntry == tEntry) {
            contact.nx = dx > 0 ? -1 : 1;
        } else if (yEntry == tEntry) {
            contact.ny = dy > 0 ? -1 : 1;
        } else if (zEntry == tEntry) {
            contact.nz = dz > 0 ? -1 : 1;
        }
        contacts.add(contact);
    }

    /**
     * Respond to all found collision contacts.
     */
    public void collisionResponse(float dt, Vector3f velocity, Vector4f position, List<Contact> contacts) {
        sort(contacts);
        int minX = Integer.MIN_VALUE,
                maxX = Integer.MAX_VALUE,
                minY = Integer.MIN_VALUE,
                maxY = Integer.MAX_VALUE,
                minZ = Integer.MIN_VALUE,
                maxZ = Integer.MAX_VALUE;
        float elapsedTime = 0f;
        float dx = velocity.x * dt,
                dy = velocity.y * dt,
                dz = velocity.z * dt;
        for (Contact contact : contacts) {
            if (contact.x <= minX || contact.y <= minY
                    || contact.z <= minZ || contact.x >= maxX
                    || contact.y >= maxY || contact.z >= maxZ) {
                continue;
            }

            float t = contact.t - elapsedTime;
            velocity.add(dx * t * 2, dy * t * 2, dz * t * 2);
            elapsedTime += t;
            if (contact.nx != 0) {
                minX = dx < 0 ? max(minX, contact.x) : minX;
                maxX = dx < 0 ? maxX : min(maxX, contact.x);
                velocity.x = 0f;
                dx = 0f;
            } else if (contact.ny != 0) {
                minY = dy < 0 ? max(minY, contact.y) : contact.y - (int) Player.PLAYER_HEIGHT;
                maxY = dy < 0 ? contact.y + (int) ceil(Player.PLAYER_HEIGHT) + 1 : min(maxY, contact.y);
                velocity.y = 0f;
                dy = 0f;
            } else if (contact.nz != 0) {
                minZ = dz < 0 ? max(minZ, contact.z) : minZ;
                maxZ = dz < 0 ? maxZ : min(maxZ, contact.z);
                velocity.z = 0f;
                dz = 0f;
            }
        }
        float trem = 1f - elapsedTime;
        velocity.add(dx * trem * 2, dy * trem * 2, dz * trem * 2);
    }

    /**
     * Create a chunk at the position <code>(cx, cz)</code> (in units of whole chunks).
     *
     * @param cx the x coordinate of the chunk position
     * @param cz the z coordinate of the chunk position
     */
    private Chunk createChunk(int cx, int cz) {
        Chunk chunk = new Chunk(cx, cz);
        return chunk;
    }

    public boolean containsChunk(Vector3f position) {
        int xOffset = (int) position.x;
        int yOffset = (int) position.y;
        int zOffset = (int) position.z;
        return containsChunk(xOffset, yOffset, zOffset);
    }

    public boolean containsChunk(int xPosition, int yPosition, int zPosition) {
        int xOffset = xPosition / WORLD_WIDTH;
        int yOffset = yPosition / WORLD_WIDTH;
        int zOffset = zPosition / WORLD_WIDTH;
        String chunkKey = String.format("%s:%s:%s", xOffset, yOffset, zOffset);
        return chunkMap.containsKey(chunkKey);
    }

    public Chunk getChunk(Vector3f position) {
        int xOffset = (int) position.x;
        int yOffset = (int) position.y;
        int zOffset = (int) position.z;
        return getChunk(xOffset, yOffset, zOffset);
    }

    public Chunk getChunk(int xPosition, int yPosition, int zPosition) {
        int xOffset = xPosition / WORLD_WIDTH;
        int yOffset = yPosition / WORLD_WIDTH;
        int zOffset = zPosition / WORLD_WIDTH;

        String chunkKey = String.format("%s:%s:%s", xOffset, yOffset, zOffset);
        return chunkMap.get(chunkKey);
    }

    public Chunk addChunk(Vector3f position) {
        int xOffset = (int) position.x;
        int yOffset = (int) position.y;
        int zOffset = (int) position.z;
        return addChunk(xOffset, yOffset, zOffset);
    }

    public Chunk addChunk(int xPosition, int yPosition, int zPosition) {
        int xOffset = xPosition / WORLD_WIDTH;
        int yOffset = yPosition / WORLD_WIDTH;
        int zOffset = zPosition / WORLD_WIDTH;

        String chunkKey = String.format("%s:%s:%s", xOffset, yOffset, zOffset);
        Chunk chunk = new Chunk(xOffset, yOffset, zOffset);
        chunkMap.put(chunkKey, chunk);
        return chunk;
    }

    public void cleanup() {
        chunkMap.forEach((key, value) -> value.cleanup());
        this.chunkMap.clear();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2000L, TimeUnit.MILLISECONDS))
                throw new AssertionError();
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    public Map<String, Chunk> getChunkMap() {
        return chunkMap;
    }

    /**
     * Return the flattened chunk field index <code>(x, z)</code>.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public String idx(int x, int z) {
        StringBuilder key = new StringBuilder()
                .append(x)
                .append(":")
                .append(z);
        return key.toString();
    }
}

package ru.mycubecraft.world;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.data.Contact;
import ru.mycubecraft.engine.graph.CollisionPrediction;
import ru.mycubecraft.world.player.Player;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
     * The number of chunks, starting from the player's position, that should be visible in any given
     * direction.
     */
    private static final int MAX_RENDER_DISTANCE_CHUNKS = 40;

    /**
     * The maximum render distance in meters.
     */
    private static final int MAX_RENDER_DISTANCE_METERS = MAX_RENDER_DISTANCE_CHUNKS << CHUNK_SIZE_SHIFT;

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

    private final Map<String, Chunk> chunkMap = new ConcurrentHashMap<>();

    public World() {
    }


    public void generateStartChunks() {

    }

    private Chunk generateChunk(int cx, int cz) {
        String chunkKey = idx(cx, cz);
        Chunk chunk;
        if (!chunkMap.containsKey(chunkKey)) {
            chunk = createChunk(cx, cz);
            chunkMap.put(chunkKey, chunk);
            executorService.submit(() -> {
                try {
                    chunk.createBlockField();
                    chunk.sortBlocksVisibility();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return chunk;
        }

        return null;
    }

    /**
     * Detect possible collision candidates.
     */
    public void collisionDetection(float dt, Vector3f velocity, Player player, List<Contact> contacts) {

        Vector3f position = new Vector3f(player.getPosition().x, player.getPosition().y, player.getPosition().z);

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

                    float distX = position.x - x;
                    float distY = position.y - y;
                    float distZ = position.z - z;

                    /* and perform swept-aabb intersection */
                    CollisionPrediction.intersectSweptAabbAabb(x, y, z, distX, distY, distZ, dx, dy, dz, contacts, blockField);
                }
            }
        }
    }


    /**
     * Respond to all found collision contacts.
     */
    public void collisionResponse(float dt, Vector3f velocity, Player player, List<Contact> contacts) {
        sort(contacts);
        int minX = Integer.MIN_VALUE, maxX = Integer.MAX_VALUE, minY = Integer.MIN_VALUE, maxY = Integer.MAX_VALUE, minZ = Integer.MIN_VALUE,
                maxZ = Integer.MAX_VALUE;
        float elapsedTime = 0f;
        float dx = velocity.x * dt, dy = velocity.y * dt, dz = velocity.z * dt;
        for (Contact contact : contacts) {
            if (contact.x <= minX || contact.y <= minY
                    || contact.z <= minZ || contact.x >= maxX
                    || contact.y >= maxY || contact.z >= maxZ) {
                continue;
            }

            float t = contact.t - elapsedTime;
            player.movePosition(dx * t, dy * t, dz * t);
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
        //player.movePosition(dx * trem, dy * trem, dz * trem);
        velocity.add(dx * trem, dy * trem, dz * trem);
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

    public Chunk getChunk(int xPosition, int zPosition) {
        int cx = xPosition >> CHUNK_SIZE_SHIFT,
                cz = zPosition >> CHUNK_SIZE_SHIFT;
        String chunkKey = idx(cx, cz);
        return chunkMap.get(chunkKey);
    }
//
//    public Chunk addChunk(Vector3f position) {
//        int xOffset = (int) position.x;
//        int yOffset = (int) position.y;
//        int zOffset = (int) position.z;
//        return addChunk(xOffset, yOffset, zOffset);
//    }
//
//    public Chunk addChunk(int xPosition, int yPosition, int zPosition) {
//        int xOffset = xPosition / WORLD_WIDTH;
//        int yOffset = yPosition / WORLD_WIDTH;
//        int zOffset = zPosition / WORLD_WIDTH;
//
//        String chunkKey = String.format("%s:%s:%s", xOffset, yOffset, zOffset);
//        Chunk chunk = new Chunk(xOffset, yOffset, zOffset);
//        chunkMap.put(chunkKey, chunk);
//        return chunk;
//    }

    /**
     * Ensure that a frontier neighbor chunk is created if it is visible.
     *
     * @param xPosition the x position of player
     * @param zPosition the z position of player
     */
    public void ensureChunkIfVisible(int xPosition, int zPosition) {

        int cx = xPosition >> CHUNK_SIZE_SHIFT,
                cz = zPosition >> CHUNK_SIZE_SHIFT;

        if (chunkInRenderDistance(cx, cz, xPosition, zPosition)) {
            ensureChunk(cx, cz);
        }
        if (chunkInRenderDistance(cx - 1, cz, xPosition, zPosition)) {
            ensureChunk(cx - 1, cz);
        }
        if (chunkInRenderDistance(cx + 1, cz, xPosition, zPosition)) {
            ensureChunk(cx + 1, cz);
        }
        if (chunkInRenderDistance(cx, cz - 1, xPosition, zPosition)) {
            ensureChunk(cx, cz - 1);
        }
        if (chunkInRenderDistance(cx, cz + 1, xPosition, zPosition)) {
            ensureChunk(cx, cz + 1);
        }
    }

    public Chunk ensureChunk(int xOffset, int zOffset) {
        return generateChunk(xOffset, zOffset);
    }


    /**
     * Compute the distance from the player's position to the center of the chunk at
     * <code>(xOffset, zOffset)</code>.
     */
    private double distToChunk(int xOffset, int zOffset, float xPosition, float zPosition) {
        double dx = xPosition - (xOffset + 0.5) * CHUNK_SIZE;
        double dz = zPosition - (zOffset + 0.5) * CHUNK_SIZE;
        return dx * dx + dz * dz;
    }

    /**
     * Determine whether the chunk at <code>(xOffset, zOffset)</code> is within render distance.
     */
    private boolean chunkInRenderDistance(int xOffset, int zOffset, float xPosition, float zPosition) {
        return distToChunk(xOffset, zOffset, xPosition, zPosition) < MAX_RENDER_DISTANCE_METERS << 1;
    }

    /**
     * Iterate through all current  chunks and check, whether any of them is further than the
     * {@link #MAX_RENDER_DISTANCE_CHUNKS} aways, in which case those will be destroyed.
     */
    public void destroyOutOfRenderDistanceFrontierChunks(int xPosition, int zPosition) {
        for (Iterator<Chunk> chunkIterator = chunkMap.values().iterator(); chunkIterator.hasNext(); ) {
            Chunk chunk = chunkIterator.next();
            if (!chunkInRenderDistance(chunk.getCx(), chunk.getCz(), xPosition, zPosition)) {
                chunk.cleanup();
                chunkIterator.remove();
            }
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

    public void cleanup() {
        if (!chunkMap.isEmpty()) {
            chunkMap.forEach((key, value) -> value.cleanup());
            chunkMap.clear();
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3000L, TimeUnit.MILLISECONDS)) {
                throw new AssertionError();
            }
        } catch (Exception e) {
            throw new AssertionError();
        }
    }
}

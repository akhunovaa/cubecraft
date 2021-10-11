package ru.mycubecraft.world;

import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import ru.mycubecraft.engine.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;

@Slf4j
public class World {

    // 256 chunks & 4096 blocks in one chunk & totally 1 048 576 blocks
    public static final int WORLD_WIDTH = 8;
    public static final int WORLD_HEIGHT = 8;
    public static final int WORLD_SIZE = 5 * 8; // 4=64 5=100 6=144

    /**
     * The width and depth of a chunk (in number of voxels).
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

    private final Map<String, Chunk> chunkMap = Utils.createLRUMap(166);

    public World(Generator generator) {
        generateStartChunks();
    }
//
//    public void render() {
//        chunkMap.forEach((key, value) -> value.render());
//    }

//    public List<GameItem> getChunksBlockItems() {
//        List<GameItem> gameItemList = new ArrayList<>();
//        chunkMap.forEach((key, value) -> gameItemList.addAll(value.getItemListForRendering()));
//        return gameItemList;
//    }

//    public void generate(int xPosition, int yPosition, int zPosition) {
//        int xOffset = xPosition / WORLD_WIDTH;
//        int yOffset = yPosition / WORLD_HEIGHT;
//        int zOffset = zPosition / WORLD_WIDTH;
//
//        String chunkKey = String.format("%s:%s:%s", xOffset, yOffset, zOffset);
//        if (!chunkMap.containsKey(chunkKey)) {
//            //generateChunk(xPosition, yPosition, zPosition);
//        }
//    }

    public void ensureChunk(int xPosition, int zPosition) {
        int cx = xPosition >> CHUNK_SIZE_SHIFT,
                cz = zPosition >> CHUNK_SIZE_SHIFT;

        String chunkKey = idx(cx, cz);
        if (!chunkMap.containsKey(chunkKey)) {

            Chunk chunk = createChunk(cx, cz);
            /*
             * Submit async task to create the chunk.
             */
            executorService.submit(() -> {
                try {
                    chunk.createBlockField();
                    chunk.sortBlocksVisibility();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            chunkMap.put(chunkKey, chunk);
        }
    }

    private void generateStartChunks() {
//        executorService.execute(new Runnable() {
//            public void run() {
//               generateChunk(3, 3);
//            }
//        });
    }

//    private void generateChunk(int xPosition, int yOffset, int zPosition) {
//        for (int x = (xPosition - WORLD_SIZE) / WORLD_WIDTH; x < (xPosition + WORLD_SIZE) / WORLD_WIDTH; x++) {
//            for (int z = (zPosition - WORLD_SIZE) / WORLD_WIDTH; z < (zPosition + WORLD_SIZE) / WORLD_WIDTH; z++) {
//                String chunkKey = String.format("%s:%s:%s", x, yOffset / WORLD_WIDTH, z);
//                if (!chunkMap.containsKey(chunkKey)) {
//                    Chunk chunk = new Chunk(x, z, new BasicGen(3));
//                    //chunk.generateBlocks();
//                }
//            }
//        }
//    }

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

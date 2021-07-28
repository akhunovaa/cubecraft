package ru.mycubecraft.world;

import lombok.extern.slf4j.Slf4j;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class World {

    // 256 chunks & 4096 blocks in one chunk & totally 1 048 576 blocks
    public static final int WORLD_WIDTH = 16;
    public static final int WORLD_HEIGHT = 16;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private final Map<String, Chunk> chunkMap = new ConcurrentHashMap<>();

    private final Generator generator;

    public World(Generator generator) {
        this.generator = generator;
        generateStartChunks();
    }

    public void render() {
        chunkMap.forEach((key, value) -> value.render());
    }

    public ArrayList<GameItem> getChunksBlockItems() {
        List<GameItem> gameItemList = new ArrayList<>();
        chunkMap.forEach((key, value) -> gameItemList.addAll(value.getItemListForRendering()));
        return new ArrayList<>(gameItemList);
    }

    public void generate(int xPosition, int zPosition) {
        int xOffset = xPosition / WORLD_WIDTH;
        int zOffset = zPosition / WORLD_WIDTH;

//        generateChunk(xOffset + 1, zOffset + 1);
//        removeChunk(xOffset - 1, zOffset - 1);
    }

    private void generateStartChunks() {
        // spawn center chunk
        generateChunk(0, 0);
        // spawn chunks to left
        generateChunk(-1, 0);
        generateChunk(-2, 0);
        //spawn chunks to left-front
        generateChunk(-1, -1);
        generateChunk(-2, -1);
        generateChunk(-1, -2);
        generateChunk(-2, -2);
        //spawn chunks to left-backward
        generateChunk(-1, 1);
        generateChunk(-2, 1);
        generateChunk(-1, 2);
        generateChunk(-2, 2);
        // spawn chunks to right
        generateChunk(1, 0);
        generateChunk(2, 0);
        //spawn chunks to right-front
        generateChunk(1, -1);
        generateChunk(2, -1);
        generateChunk(1, -2);
        generateChunk(2, -2);
        //spawn chunks to right-backward
        generateChunk(1, 1);
        generateChunk(2, 1);
        generateChunk(1, 2);
        generateChunk(2, 2);
        // spawn chunks to forward
        generateChunk(0, -1);
        generateChunk(0, -2);
        // spawn chunks to backward
        generateChunk(0, 1);
        generateChunk(0, 2);
    }

    private void generateChunk(int xOffset, int zOffset) {
            if (xOffset < WORLD_WIDTH && zOffset < WORLD_WIDTH) {
                String chunkKey = String.format("%s:%s", xOffset, zOffset);
                if (!chunkMap.containsKey(chunkKey)) {
                    Chunk chunk = new Chunk(xOffset, zOffset, new BasicGen(1));
                    chunkMap.put(chunkKey, chunk);
                    System.out.println("Chunk Generating Task has been Started");
                    executorService.execute(new Runnable() {
                        public void run() {
                            chunk.generateBlocks();
                        }
                    });

                }
            }
        }

    private void removeChunk(int xOffset, int zOffset) {
        String chunkKey = String.format("%s:%s", xOffset, zOffset);
        if (chunkMap.containsKey(chunkKey)){
            System.out.println("Chunk Removing Task has been Started");
            chunkMap.remove(chunkKey);
        }
    }


    public void cleanup() {
        chunkMap.forEach((key, value) -> value.cleanup());
        this.chunkMap.clear();
    }
}

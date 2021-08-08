package ru.mycubecraft.world;

import lombok.extern.slf4j.Slf4j;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.engine.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class World {

    // 256 chunks & 4096 blocks in one chunk & totally 1 048 576 blocks
    public static final int WORLD_WIDTH = 16;
    public static final int WORLD_HEIGHT = 8;
    public static final int WORLD_SIZE = 6 * 8; // 4=64 5=100 6=144

    private final ExecutorService executorService = Executors.newFixedThreadPool(16);

    private final Map<String, Chunk> chunkMap = Utils.createLRUMap(200);

    public World(Generator generator) {
        generateStartChunks();
    }

    public void render() {
        chunkMap.forEach((key, value) -> value.render());
    }

    public List<GameItem> getChunksBlockItems() {
        List<GameItem> gameItemList = new ArrayList<>();
        chunkMap.forEach((key, value) -> gameItemList.addAll(value.getItemListForRendering()));
        return gameItemList;
    }

    public void generate(int xPosition, int zPosition) {
            generateChunk(xPosition, zPosition);
    }

    private void generateStartChunks() {
//        executorService.execute(new Runnable() {
//            public void run() {
//               generateChunk(3, 3);
//            }
//        });
    }

    private void generateChunk(int xPosition, int zPosition) {
//        if (xPosition < WORLD_SIZE && zPosition < WORLD_SIZE) {
        for (int x = (xPosition - WORLD_SIZE) / 8; x < (xPosition + (WORLD_SIZE)) / 8; x++) {
            for (int z = (zPosition - WORLD_SIZE) / 8; z < (zPosition + (WORLD_SIZE)) / 8; z++) {
                String chunkKey = String.format("%s:%s", x, z);
                if (!chunkMap.containsKey(chunkKey)) {
                    Chunk chunk = new Chunk(x, z, new BasicGen(3));
                    chunk.generateBlocks();
                    chunkMap.put(chunkKey, chunk);
                }
            }
        }
    }


    public void cleanup() {
        chunkMap.forEach((key, value) -> value.cleanup());
        this.chunkMap.clear();
    }

    public Map<String, Chunk> getChunkMap() {
        return chunkMap;
    }
}

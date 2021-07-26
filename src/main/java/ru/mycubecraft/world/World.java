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

        // spawn chunks
        generateChunk(-1, -1);
        generateChunk(0, 0);
        generateChunk(1, 1);

//        generateChunk(0, -1);
//        generateChunk(-1, 0);

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

        generateChunk(xOffset + 1, zOffset + 1);
        removeChunk(xOffset - 1, zOffset - 1);

        generateChunk(xOffset, zOffset + 1);
        removeChunk(xOffset, zOffset - 1);

        generateChunk(xOffset + 1, zOffset);
        removeChunk(xOffset - 1, zOffset);
    }

    private void generateChunk(int xOffset, int zOffset) {
        String chunkKey = String.format("%s:%s", xOffset, zOffset);
        if (!chunkMap.containsKey(chunkKey)){
            if (xOffset < WORLD_WIDTH && zOffset < WORLD_WIDTH) {
                executorService.execute(new Runnable() {
                    public void run() {
                        System.out.println("Chunk Generating Task Execute Started");
                        Chunk chunk = new Chunk(xOffset, zOffset, new BasicGen(1));
                        chunk.generateBlocks();
                        chunkMap.put(chunkKey, chunk);
                    }
                });
            }
        }
    }

    private void removeChunk(int xOffset, int zOffset) {
        String chunkKey = String.format("%s:%s", xOffset, zOffset);
        if (chunkMap.containsKey(chunkKey)){
            if (xOffset < WORLD_WIDTH && zOffset < WORLD_WIDTH) {
                executorService.execute(new Runnable() {
                    public void run() {
                        System.out.println("Chunk Removing Task Execute Started");
                        chunkMap.remove(chunkKey);
                    }
                });
            }
        }
    }


    public void cleanup() {
        chunkMap.forEach((key, value) -> value.cleanup());
        this.chunkMap.clear();
    }
}

package ru.mycubecraft.world;

import lombok.extern.slf4j.Slf4j;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class World {

    // 256 chunks & 4096 blocks in one chunk & totally 1 048 576 blocks
    public static final int WORLD_WIDTH = 16;
    public static final int WORLD_HEIGHT = 16;

    //    private final Chunk[][] chunks = new Chunk[WORLD_WIDTH][WORLD_WIDTH];
    private final HashSet<Chunk> chunkSet = new HashSet<>();

    private final Generator generator;

    public World(Generator generator) {
        this.generator = generator;

        // spawn chunks
        generateChunk(-1, -1);
        generateChunk(0, -1);
        generateChunk(-1, 0);
        generateChunk(0, 0);

    }

    public void render() {
        chunkSet.forEach(Chunk::render);
    }

    public ArrayList<GameItem> getChunksBlockItems() {
        List<GameItem> gameItemList = new ArrayList<>();
        for (Chunk chunk : chunkSet) {
            gameItemList.addAll(chunk.getItemListForRendering());
        }
        return new ArrayList<>(gameItemList);
    }

    public void generate() {
//        generateChunk(WORLD_WIDTH / 2, WORLD_WIDTH / 2);
//        generateChunk(WORLD_WIDTH / 2 - 1, WORLD_WIDTH / 2 - 1);
//        generateChunk(WORLD_WIDTH / 2 - 2, WORLD_WIDTH / 2 - 2);
//        generateChunk(1, 1);
        //generateChunk(0, 1);
    }

    private void generateChunk(int cX, int cZ) {
        if (cX < WORLD_WIDTH && cZ < WORLD_WIDTH) {
            Chunk chunk = new Chunk(cX, cZ, this.generator);
            chunk.generateBlocks();
            chunkSet.add(chunk);
        }
    }


    public void cleanup() {
        chunkSet.forEach(Chunk::cleanup);
    }
}

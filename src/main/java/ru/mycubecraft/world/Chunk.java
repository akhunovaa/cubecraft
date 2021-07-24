package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;

@Getter
@EqualsAndHashCode
public class Chunk {

    private static final int BLOCKS_COUNT = 16;

    private final int cx; //offset to x (1 offset * 16 block count)
    private final int cz; //offset to z (1 offset * 16 block count)

    @EqualsAndHashCode.Exclude
    private final Block[][][] blocks;

    @EqualsAndHashCode.Exclude
    private Generator generator;

    public Chunk(int cx, int cz) {
        this.cx = cx;
        this.cz = cz;
        this.blocks = new Block[BLOCKS_COUNT][World.WORLD_HEIGHT][BLOCKS_COUNT];
    }

    public Chunk(int cx, int cz, Generator generator) {
        this.cx = cx;
        this.cz = cz;
        this.blocks = new Block[BLOCKS_COUNT][World.WORLD_HEIGHT][BLOCKS_COUNT];
        this.generator = generator;

    }

    public void generateBlocks() {
        int wX = this.cx * BLOCKS_COUNT; // block position where cx is offset and BLOCKS_COUNT is block count to X (1 offset * 16 block count)
        int wZ = this.cz * BLOCKS_COUNT; // block position where cz is offset and BLOCKS_COUNT is block count to Z (1 offset * 16 block count)

        // every block in chunk took this (new Vector3f(wX, 0, wZ)) position
        // time complexity is O(n^2) exclude Y coordinate (height) and O(n^3) with Y coordinate
        for (int x = 0; x < BLOCKS_COUNT; x++) { // iterating & creating blocks for X coordinate in this chunk
            for (int z = 0; z < BLOCKS_COUNT; z++) { // iterating & creating blocks for Z coordinate in this chunk
                int mHeight = generator.maxHeight(wX + x, wZ + z, 0);
                for (int y = 0; y < World.WORLD_HEIGHT; y++) {
                    if (y < mHeight) {
                        Block block = generator.genBlock(wX + x, y, wZ + z, 0);
                        this.blocks[x][y][z] = block;
                    } else {
                        this.blocks[x][y][z] = null;
                    }

                }
            }
        }
    }

    public void render() {
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (this.blocks[x][y][z] != null) {
                        this.blocks[x][y][z].render();
                    }

                }
            }
        }
    }

    public ArrayList<GameItem> getItemListForRendering() {
        ArrayList<GameItem> gameItemList = new ArrayList<>();
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (this.blocks[x][y][z] != null) {
                        GameItem gameItem = this.blocks[x][y][z].getGameCubeItem();
                        gameItemList.add(gameItem);
                    }
                }
            }
        }
        return gameItemList;
    }

    public void cleanup() {
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (this.blocks[x][y][z] != null) {
                        GameItem gameItem = this.blocks[x][y][z].getGameCubeItem();
                        gameItem.cleanup();
                    }
                }
            }
        }

    }
}

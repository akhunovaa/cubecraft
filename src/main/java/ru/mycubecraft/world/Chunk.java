package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joml.Vector3f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;

@Getter
@EqualsAndHashCode
public class Chunk {

    private static final int BLOCKS_COUNT = 8;

    private final int cx; //offset to x (1 offset * 8 block count)
    private final int cy; //offset to y (1 offset * 8 block count)
    private final int cz; //offset to z (1 offset * 16 block count)

    @EqualsAndHashCode.Exclude
    private final Block[][][] blocks;

    @EqualsAndHashCode.Exclude
    private Generator generator;

    public Chunk(int cx, int cz) {
        this.cx = cx;
        this.cy = 0;
        this.cz = cz;
        this.blocks = new Block[BLOCKS_COUNT][BLOCKS_COUNT][BLOCKS_COUNT];
    }

    public Chunk(int cx, int cz, Generator generator) {
        this.cx = cx;
        this.cz = cz;
        this.cy = 0;
        this.blocks = new Block[BLOCKS_COUNT][BLOCKS_COUNT][BLOCKS_COUNT];
        this.generator = generator;
    }

    public Chunk(int cx, int cy, int cz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.blocks = new Block[BLOCKS_COUNT][BLOCKS_COUNT][BLOCKS_COUNT];
    }

    public Chunk(int cx, int cy, int cz, Generator generator) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.blocks = new Block[BLOCKS_COUNT][BLOCKS_COUNT][BLOCKS_COUNT];
        this.generator = generator;
    }

    public void generateBlocks() {
        int wX = this.cx * BLOCKS_COUNT; // block position where cx is offset and BLOCKS_COUNT is block count to X (1 offset * 8 block count)
        int wY = this.cy * BLOCKS_COUNT; // block position where cy is offset and BLOCKS_COUNT is block count to Y (1 offset * 8 block count)
        int wZ = this.cz * BLOCKS_COUNT; // block position where cz is offset and BLOCKS_COUNT is block count to Z (1 offset * 8 block count)

        // every block in chunk took this (new Vector3f(wX, 0, wZ)) position
        // time complexity is O(n^2) exclude Y coordinate (height) and O(n^3) with Y coordinate
        for (int x = 0; x < BLOCKS_COUNT; x++) { // iterating & creating blocks for X coordinate in this chunk
            for (int z = 0; z < BLOCKS_COUNT; z++) { // iterating & creating blocks for Z coordinate in this chunk
                int mHeight = generator.maxHeight(wX + x, wZ + z, 0);
                for (int y = 0; y < BLOCKS_COUNT; y++) {
                    if (y < mHeight) {
                        Block block = generator.genBlock(wX + x, wY + y, wZ + z, 0);
                        this.blocks[x][y][z] = block;
                    } else {
                        this.blocks[x][y][z] = null;
                    }

                }
            }
        }
    }

    public boolean containsBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;

        return this.blocks[xPosition][yPosition][zPosition] != null;
    }

    public Block addBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;

        Block block = generator.genBlock(xPosition, yPosition, zPosition, 0);
        this.blocks[xPosition][yPosition][zPosition] = block;
        return block;
    }

    public boolean deleteBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;

        this.blocks[xPosition][yPosition][zPosition] = null;
        return true;
    }

    public void render() {
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
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
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
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
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    if (this.blocks[x][y][z] != null) {
                        GameItem gameItem = this.blocks[x][y][z].getGameCubeItem();
                        gameItem.cleanup();
                    }
                }
            }
        }

    }
}

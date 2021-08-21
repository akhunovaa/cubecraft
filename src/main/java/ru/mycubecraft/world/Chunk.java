package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
public class Chunk {

    private static final int BLOCKS_COUNT = 8;

    private final int cx; //offset to x (1 offset * 8 block count)
    private final int cy; //offset to y (1 offset * 8 block count)
    private final int cz; //offset to z (1 offset * 8 block count)
    @EqualsAndHashCode.Exclude
    private final Map<String, Block> blocks = new HashMap<>(512);
    private boolean full;
    @EqualsAndHashCode.Exclude
    private Generator generator;

    public Chunk(int cx, int cz) {
        this.cx = cx;
        this.cy = 0;
        this.cz = cz;
    }

    public Chunk(int cx, int cy, int cz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.generator = new BasicGen(3);
    }

    public Chunk(int cx, int cz, Generator generator) {
        this.cx = cx;
        this.cz = cz;
        this.cy = 0;
        this.generator = generator;
    }

    public Chunk(int cx, int cy, int cz, Generator generator) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
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
                    String blockKey = String.format("%s:%s:%s", wX + x, wY + y, wZ + z);
                    if (y < mHeight) {
                        Block block = generator.genBlock(wX + x, wY + y, wZ + z, 0);
                        this.blocks.put(blockKey, block);
                    }
                }
            }
        }
    }

    public boolean containsBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;

        String blockKey = String.format("%s:%s:%s", xPosition, yPosition, zPosition);
        return this.blocks.containsKey(blockKey);
    }

    public Block addBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;

        String blockKey = String.format("%s:%s:%s", xPosition, yPosition, zPosition);
        Block block = generator.genBlock(xPosition, yPosition, zPosition);

        this.blocks.put(blockKey, block);
        return block;
    }

    public boolean deleteBlock(Vector3f position) {
        int xPosition = (int) position.x;
        int yPosition = (int) position.y;
        int zPosition = (int) position.z;
        String blockKey = String.format("%s:%s:%s", xPosition, yPosition, zPosition);
        this.blocks.remove(blockKey);
        return true;
    }

    public void render() {
        this.blocks.forEach((key, value) -> value.render());
    }

    public ArrayList<GameItem> getItemListForRendering() {
        ArrayList<GameItem> gameItemList = new ArrayList<>(this.blocks.size());
        this.blocks.forEach((key, value) -> gameItemList.add(value.getGameCubeItem()));
        return gameItemList;
    }

    public void cleanup() {
        this.blocks.forEach((key, value) -> value.getGameCubeItem().cleanup());

    }
}

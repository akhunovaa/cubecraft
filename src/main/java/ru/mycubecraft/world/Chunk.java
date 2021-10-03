package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.DirtBlock;
import ru.mycubecraft.block.GrassBlock;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joml.SimplexNoise.noise;

@Getter
@Setter
@EqualsAndHashCode
public class Chunk {

    private static final int BLOCKS_COUNT = 8;

    /**
     * The chunk offset for the noise function.
     */
    private static final int GLOBAL_X = 2500, GLOBAL_Z = 851;

    /**
     * The width, height and depth of a chunk (in number of blocks).
     */
    private static final int CHUNK_HEIGHT = 256;
    private static final int CHUNK_SIZE_SHIFT = 5;
    private static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;

    /**
     * The minimum height for the generated terrain.
     */
    private static final int BASE_Y = 50;

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
        long time = System.currentTimeMillis();
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

    /**
     * Create a block field for a chunk at the given chunk position.
     */
    public BlockField createBlockField() {
        int gx = (cx << CHUNK_SIZE_SHIFT) + GLOBAL_X,
                gz = (cz << CHUNK_SIZE_SHIFT) + GLOBAL_Z;
        Block[] field = new Block[(CHUNK_SIZE + 2) * (CHUNK_HEIGHT + 2) * (CHUNK_SIZE + 2)];
        int maxY = Integer.MIN_VALUE,
                minY = Integer.MAX_VALUE;
        int num = 0;
        for (int z = -1; z < CHUNK_SIZE + 1; z++) {
            for (int x = -1; x < CHUNK_SIZE + 1; x++) {
                int y = (int) terrainNoise(gx + x, gz + z);
                y = min(max(y, 0), CHUNK_HEIGHT - 1);
                maxY = max(maxY, y);
                minY = min(minY, y);
                for (int y0 = -1; y0 <= y; y0++) {
                    field[idx(x, y0, z)] = y0 == y ? new GrassBlock(x, y0, z) : new DirtBlock(x, y0, z);
                    num++;
                }
            }
        }
        BlockField blockField = new BlockField();
        blockField.setNy(minY);
        blockField.setPy(maxY);
        blockField.setNum(num);
        blockField.setField(field);
        return blockField;
    }

    /**
     * Return the flattened block field index for a local block at <code>(x, y, z)</code>.
     */
    private static int idx(int x, int y, int z) {
        return (x + 1) + (32 + 2) * ((z + 1) + (y + 1) * (32 + 2));
    }

    /**
     * Evaluate a heightmap/terrain noise function at the given global <code>(x, z)</code> position.
     */
    private static float terrainNoise(int x, int z) {
        float xzScale = 0.0018f;
        float ampl = 255;
        float y = 0;
        float groundLevel = BASE_Y + noise(x * xzScale, z * xzScale) * ampl * 0.1f;
        for (int i = 0; i < 4; i++) {
            y += ampl * (noise(x * xzScale, z * xzScale) * 0.5f + 0.2f);
            ampl *= 0.42f;
            xzScale *= 2.2f;
        }
        y = min(CHUNK_HEIGHT - 2, max(y, groundLevel));
        return y;
    }

    public void cleanup() {
        this.blocks.forEach((key, value) -> value.getGameCubeItem().cleanup());

    }
}

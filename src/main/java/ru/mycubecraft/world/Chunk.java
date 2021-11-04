package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.DirtBlock;
import ru.mycubecraft.block.GrassBlock;
import ru.mycubecraft.renderer.cube.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joml.SimplexNoise.noise;

@Slf4j
@Getter
@Setter
@EqualsAndHashCode
public class Chunk {

    /**
     * The width, height and depth of a chunk (in number of blocks).
     */
    public static final int CHUNK_HEIGHT = 256;
    private static final int BLOCKS_COUNT = 8;
    /**
     * The chunk offset for the noise function.
     */
    private static final int GLOBAL_X = 2500, GLOBAL_Z = 851;
    private static final int CHUNK_SIZE_SHIFT = 5;
    private static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;

    /**
     * The minimum height for the generated terrain.
     */
    private static final int BASE_Y = 50;

    private final int cx; //offset to x (1 offset * 8 block count)
    private final int cy; //offset to y (1 offset * 8 block count)
    private final int cz; //offset to z (1 offset * 8 block count)
    //    @EqualsAndHashCode.Exclude
//    private final Map<String, Block> blocks = new HashMap<>();
    @EqualsAndHashCode.Exclude
    private BlockField blockField;

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

    /**
     * Create a block field for a chunk at the given chunk position.
     */
    public void createBlockField() {
        int gx = (this.cx << CHUNK_SIZE_SHIFT) + GLOBAL_X,
                gz = (this.cz << CHUNK_SIZE_SHIFT) + GLOBAL_Z;

        // block position where cx is offset and CHUNK_SIZE is block count to X (1 offset * 32 block count)
        int wX = this.cx * CHUNK_SIZE;
        // block position where cz is offset and CHUNK_SIZE is block count to Z (1 offset * 32 block count)
        int wZ = this.cz * CHUNK_SIZE;

        Map<String, Block> field = new HashMap<>((CHUNK_SIZE + 2) * (CHUNK_HEIGHT + 2) * (CHUNK_SIZE + 2));
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
                    String key = idx(x + wX, y0, z + wZ);
                    Block block = new DirtBlock(x + wX, y0, z + wZ);
                    field.put(key, block);
                    num++;
                }
//                for (int y0 = CHUNK_HEIGHT - 1; y0 > y; y0--) {
//                    String key = idx(x + wX, y0, z + wZ);
//                    Block block = new EmptyBlock(x + wX, y0, z + wZ);
//                    field.put(key, block);
//                    num++;
//                }
            }
        }
        BlockField blockField = new BlockField();
        blockField.setNy(minY);
        blockField.setPy(maxY);
        blockField.setNum(num);
        blockField.setBlocks(field);
        this.blockField = blockField;
    }

    public void sortBlocksVisibility() {
        Set<Map.Entry<String, Block>> blocks = this.blockField.getBlocks().entrySet();
        for (Map.Entry<String, Block> blocksEntry : blocks) {
            Block block = blocksEntry.getValue();
            if (block == null) {
                continue;
            }
            Cube cube = calculateChunksBlocksFace(block);
            block.createCube(cube);
        }
    }

    public Cube calculateChunksBlocksFace(Block block) {

        if (block instanceof GrassBlock) {
            block.setVisible(true);
            return new Cube();
        }

        int xPosition = (int) block.getPosition().x;
        int yPosition = (int) block.getPosition().y;
        int zPosition = (int) block.getPosition().z;

        Block rightBlock = findNotEmptyBlock(xPosition + 1, yPosition, zPosition);
        Block rightRightBlock = findNotEmptyBlock(xPosition + 2, yPosition, zPosition);
        Block leftBlock = findNotEmptyBlock(xPosition - 1, yPosition, zPosition);
        Block frontBlock = findNotEmptyBlock(xPosition, yPosition, zPosition + 1);
        Block frontFrontBlock = findNotEmptyBlock(xPosition, yPosition, zPosition + 2);
        Block backBlock = findNotEmptyBlock(xPosition, yPosition, zPosition - 1);
        Block backBackBlock = findNotEmptyBlock(xPosition, yPosition, zPosition - 2);
        Block topBlock = findNotEmptyBlock(xPosition, yPosition + 1, zPosition);
        Block topTopBlock = findNotEmptyBlock(xPosition, yPosition + 2, zPosition);
        Block bottomBlock = findNotEmptyBlock(xPosition, yPosition - 1, zPosition);

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock != null && backBlock != null) {
            block.setVisible(true);
            return new BottomCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock == null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BottomBackLeftRightCube();
        }

        if (topBlock != null && bottomBlock != null
                && rightBlock != null && rightRightBlock == null && leftBlock == null
                && frontBlock != null && backBlock != null) {
            block.setVisible(true);
            return new RightCube();
        }

        if (topBlock != null && topTopBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock != null
                && frontBlock != null && backBlock != null) {
            block.setVisible(true);
            return new LeftCube();
        }

        if (topBlock != null && topTopBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock == null && frontFrontBlock == null && backBlock != null) {
            block.setVisible(true);
            return new FrontCube();
        }

        if (topBlock != null && backBackBlock == null && topTopBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BackCube();
        }

        if (topBlock != null && topTopBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BackCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock == null
                && frontBlock != null && backBlock != null) {
            block.setVisible(true);
            return new BottomRightCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock != null
                && frontBlock != null && backBlock != null) {
            block.setVisible(true);
            return new BottomLeftCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock == null && backBlock != null) {
            block.setVisible(true);
            return new BottomFrontCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock != null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BottomBackCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock != null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BottomBackLeftCube();
        }

        if (topTopBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock != null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BottomBackLeftCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock == null
                && frontBlock != null && backBlock == null) {
            block.setVisible(true);
            return new BottomBackRightCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock == null && leftBlock != null
                && frontBlock == null && backBlock != null) {
            block.setVisible(true);
            return new BottomFrontLeftCube();
        }

        if (topBlock == null && bottomBlock != null
                && rightBlock != null && leftBlock == null
                && frontBlock == null && backBlock != null) {
            block.setVisible(true);
            return new BottomFrontRightCube();
        }

        return new Cube();
    }

    /**
     * Return the flattened block field index for a local block at <code>(x, y, z)</code>.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public String idx(int x, int y, int z) {
        StringBuilder key = new StringBuilder()
                .append(x)
                .append(":")
                .append(y)
                .append(":")
                .append(z);
        return key.toString();
    }

    private Block findNotEmptyBlock(int xPosition, int yPosition, int zPosition) {
        Block block = this.blockField.load(xPosition, yPosition, zPosition);
        return block;
    }

    public void cleanup() {
        if (blockField != null) {
            blockField.cleanup();
        }
    }
}

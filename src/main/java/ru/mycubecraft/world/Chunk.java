package ru.mycubecraft.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.DirtBlock;

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
                    field.put(key, new DirtBlock(x + wX, y0, z + wZ));
                    num++;
                }
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
        Map<String, Block> blocks = this.blockField.getBlocks();
        blocks.values()
                .forEach(block -> {
                    int xPosition = (int) block.getPosition().x;
                    int yPosition = (int) block.getPosition().y;
                    int zPosition = (int) block.getPosition().z;
                    Block rightBlock = this.blockField.load(xPosition + 1, yPosition, zPosition);
                    Block leftBlock = this.blockField.load(xPosition - 1, yPosition, zPosition);
                    Block frontBlock = this.blockField.load(xPosition, yPosition, zPosition + 1);
                    Block backBlock = this.blockField.load(xPosition, yPosition, zPosition - 1);
                    Block topBlock = this.blockField.load(xPosition, yPosition + 1, zPosition);
                    Block topTopBlock = this.blockField.load(xPosition, yPosition + 2, zPosition);
                    Block bottomBlock = this.blockField.load(xPosition, yPosition - 1, zPosition);

                    if (topBlock == null && bottomBlock != null) {
                        block.setVisible(true);
                    } else if (topBlock != null && topTopBlock == null && rightBlock == null) {
                        block.setVisible(true);
                    } else if (topBlock != null && topTopBlock == null && leftBlock == null) {
                        block.setVisible(true);
                    } else if (topBlock != null && topTopBlock == null && frontBlock == null) {
                        block.setVisible(true);
                    } else if (topBlock != null && topTopBlock == null && backBlock == null) {
                        block.setVisible(true);
                    }
                });
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

    public void cleanup() {
        // this.blocks.forEach((key, value) -> value.getGameCubeItem().cleanup());

    }
}

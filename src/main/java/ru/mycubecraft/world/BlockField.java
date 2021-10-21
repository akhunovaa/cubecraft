package ru.mycubecraft.world;

import lombok.Getter;
import lombok.Setter;
import ru.mycubecraft.block.Block;

import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Represents the block field of a single chunk.
 */
@Getter
@Setter
public class BlockField {

    /**
     * The minimum y index containing non-zero blocks. This is used to speedup greedy meshing by
     * skipping empty area altogether.
     */
    private int ny;

    /**
     * The maximum y index containing non-zero blocks. This is used to speedup greedy meshing by
     * skipping empty area altogether.
     */
    private int py;

    /**
     * The actual block field
     */
    private Map<String, Block> blocks;

    /**
     * The number of set/active non-zero blocks. This value can be used to get a (very) rough estimate
     * of the needed faces when meshing.
     */
    private int num;

    /**
     * Return the flattened block field index for a local block at <code>(x, y, z)</code>.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private static String idx(int x, int y, int z) {
        StringBuilder key = new StringBuilder()
                .append(x)
                .append(":")
                .append(y)
                .append(":")
                .append(z);
        return key.toString();
    }

    /**
     * Stores the value 'v' into block (x, y, z).
     *
     * @param x     the local x coordinate
     * @param y     the local y coordinate
     * @param z     the local z coordinate
     * @param block the block value
     * @return this
     */
    private BlockField store(int x, int y, int z, Block block) {
        String key = idx(x, y, z);
        blocks.put(key, block);
        /*
         * Update min/max Y coordinate so that meshing as well as frustum culling will take it into account
         */
        ny = min(ny, y);
        py = max(py, y);
        return this;
    }

    /**
     * Loads the current value of the block (x, y, z).
     *
     * @param x the local x coordinate
     * @param y the local y coordinate
     * @param z the local z coordinate
     * @return the block value
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public Block load(int x, int y, int z) {
        StringBuilder key = new StringBuilder()
                .append(x)
                .append(":")
                .append(y)
                .append(":")
                .append(z);
        return blocks.get(key.toString());
    }

    public void cleanup() {
        if (blocks != null && !blocks.isEmpty()) {
            blocks.clear();
            blocks = null;
        }
    }

}

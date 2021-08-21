package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.DirtBlock;
import ru.mycubecraft.block.GrassBlock;

import java.util.Random;

public class PerlinNoiseGenerator implements Generator {

    public static float AMPLITUDE = 80f;
    public static int OCTAVES = 7;
    public static float ROUGHNESS = 0.3f;

    private final Random random = new Random();
    private final int seed;
    private int xOffset = 0;
    private int zOffset = 0;

    public PerlinNoiseGenerator(int seed) {
        this.seed = seed;
    }

    //only works with POSITIVE gridX and gridZ values!
    public PerlinNoiseGenerator(int gridX, int gridZ, int vertexCount, int seed) {
        this.seed = seed;
        xOffset = gridX * (vertexCount - 1);
        zOffset = gridZ * (vertexCount - 1);
    }

    private float getInterpolatedNoise(float x, float z) {
        int intX = (int) x;
        int intZ = (int) z;
        float fracX = x - intX;
        float fracZ = z - intZ;

        float v1 = getSmoothNoise(intX, intZ);
        float v2 = getSmoothNoise(intX + 1, intZ);
        float v3 = getSmoothNoise(intX, intZ + 1);
        float v4 = getSmoothNoise(intX + 1, intZ + 1);
        float i1 = interpolate(v1, v2, fracX);
        float i2 = interpolate(v3, v4, fracX);
        return interpolate(i1, i2, fracZ);
    }

    private float interpolate(float a, float b, float blend) {
        double theta = blend * Math.PI;
        float f = (float) (1f - Math.cos(theta)) * 0.5f;
        return a * (1f - f) + b * f;
    }

    private float getSmoothNoise(int x, int z) {
        float corners = (getNoise(x - 1, z - 1) + getNoise(x + 1, z - 1) + getNoise(x - 1, z + 1)
                + getNoise(x + 1, z + 1)) / 16f;
        float sides = (getNoise(x - 1, z) + getNoise(x + 1, z) + getNoise(x, z - 1)
                + getNoise(x, z + 1)) / 8f;
        float center = getNoise(x, z) / 4f;
        return corners + sides + center;
    }

    private float getNoise(int x, int z) {
        random.setSeed(x * 49632L + z * 325176L + seed);
        return random.nextFloat() * 2f - 1f;
    }


    @Override
    public Block genBlock(int wX, int wY, int wZ, int seed) {
        int height = (int) (getInterpolatedNoise(wX / 80.0f, wZ / 80.0f) * 5.0 + 10.0);
        return wY > height + 1 ? null : (wY > height ? new GrassBlock(wX, wY, wZ) : new DirtBlock(wX, wY, wZ));
    }

    @Override
    public Block genBlock(int wX, int wY, int wZ) {
        throw new RuntimeException("Not implemented here!");
    }

    @Override
    public int maxHeight(int wX, int wZ, int seed) {

        wX = wX < 0 ? -wX : wX;
        wZ = wZ < 0 ? -wZ : wZ;

        float total = 0;
        float d = (float) Math.pow(2, OCTAVES - 1);
        for (int i = 0; i < OCTAVES; i++) {
            float freq = (float) (Math.pow(2, i) / d);
            float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
            total += getInterpolatedNoise((wX + xOffset) * freq, (wZ + zOffset) * freq) * amp;
        }

        return (int) total;
    }
}

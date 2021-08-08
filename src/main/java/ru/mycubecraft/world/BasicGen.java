package ru.mycubecraft.world;


import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.GrassBlock;

public class BasicGen implements Generator {

    public SimplexNoiseOctave simp;

    public BasicGen(int seed) {
        simp = new SimplexNoiseOctave(seed);
    }

    @Override
    public Block genBlock(int wX, int wY, int wZ, int seed) {
        int height = (int) (simp.noise(wX / 80.0, wZ / 80.0) * 5.0 + 3.0);
        return wY > height + 1 ? null : (wY >= height ? new GrassBlock(wX, wY, wZ) : new GrassBlock(wX, wY, wZ));
    }

    @Override
    public int maxHeight(int wX, int wZ, int seed) {
        return (int) (simp.noise(wX / 80.0, wZ / 80.0) * 5.0 + 3.0) + 2;
    }


}

package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.DirtBlock;
import ru.mycubecraft.block.GrassBlock;

public class SuperFlatGen implements Generator {

    @Override
    public Block genBlock(int wX, int wY, int wZ, int seed) {
        Block b = wY > 6 ? null : (wY > 5 ? new GrassBlock(wX, wY, wZ) : new DirtBlock(wX, wY, wZ));//new EmptyBlock(wX, wY,wZ):(wY>5?new GrassBlock(wX, wY,wZ):new DirtBlock(wX,wY,wZ));
        return b;
    }

    @Override
    public int maxHeight(int wX, int wZ, int seed) {
        throw new RuntimeException("Not implemented here!");
    }

    @Override
    public Block genBlock(int wX, int wY, int wZ) {
        throw new RuntimeException("Not implemented here!");
    }

}

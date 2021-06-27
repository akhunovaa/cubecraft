package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;

public interface Generator {
    public Block genBlock(int wX, int wY, int wZ, int seed);

    public int maxHeight(int wX, int wZ, int seed);

}

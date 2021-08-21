package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;

public interface Generator {

    Block genBlock(int wX, int wY, int wZ, int seed);

    Block genBlock(int wX, int wY, int wZ);

    int maxHeight(int wX, int wZ, int seed);

}

package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;

public interface Generator {
    Block genBlock(int wX, int wY, int wZ, int seed);

    int maxHeight(int wX, int wZ, int seed);

}

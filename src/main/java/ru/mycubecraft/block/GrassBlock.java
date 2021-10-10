package ru.mycubecraft.block;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class GrassBlock extends Block {

    public GrassBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ, "assets/textures/grass.png");

        // TODO Auto-generated constructor stub
    }

}

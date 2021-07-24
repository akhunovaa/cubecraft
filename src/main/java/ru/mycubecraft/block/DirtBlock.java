package ru.mycubecraft.block;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class DirtBlock extends Block {

    public DirtBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ, "assets/textures/dirt.png");

        // TODO Auto-generated constructor stub
    }

}

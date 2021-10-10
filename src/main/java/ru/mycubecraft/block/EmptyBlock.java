package ru.mycubecraft.block;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class EmptyBlock extends Block {

    public EmptyBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ);
    }

    @Override
    public void render() {

    }
}

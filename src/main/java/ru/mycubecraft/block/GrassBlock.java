package ru.mycubecraft.block;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
@EqualsAndHashCode(callSuper = false)
public class GrassBlock extends Block {

    public GrassBlock(int bX, int bY, int bZ) {
        super(bX, bY, bZ, "assets/textures/grass.png");

        // TODO Auto-generated constructor stub
    }

    public GrassBlock(Vector3f position) {
        super(position, "assets/textures/grass.png");
    }

}

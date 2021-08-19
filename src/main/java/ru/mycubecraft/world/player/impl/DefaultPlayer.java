package ru.mycubecraft.world.player.impl;

import lombok.Getter;
import lombok.Setter;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.world.player.Player;

@Getter
@Setter
public class DefaultPlayer implements Player {

    private Camera camera;
    private Mesh mesh;
    private float scale;

    public DefaultPlayer(Camera camera) {
        this.camera = camera;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {

    }
}

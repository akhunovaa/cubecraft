package ru.mycubecraft.world.player;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.world.BlockField;
import ru.mycubecraft.world.MouseBoxSelectionDetector;

@Setter
@Getter
public abstract class Player {

    /**
     * The total height of the player's collision box.
     */
    public static final float PLAYER_HEIGHT = 2.40f;
    /**
     * The eye level of the player.
     */
    public static final float PLAYER_EYE_HEIGHT = 2.20f;
    /**
     * The width of the player's collision box.
     */
    public static final float PLAYER_WIDTH = 1.00f;
    protected boolean fly = false;
    protected boolean jumping;
    protected Matrix4f viewMatrix;
    protected Vector4f position;
    protected Vector3f rotation;
    protected MouseBoxSelectionDetector mouseBoxSelectionDetector;

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public abstract void moveRotation(double offsetX, double offsetY, double offsetZ);

    public abstract void movePosition(float offsetX, float offsetY, float offsetZ);

    public abstract Block findAndSelectBlock(BlockField blockField);
}

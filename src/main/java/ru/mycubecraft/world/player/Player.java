package ru.mycubecraft.world.player;

import lombok.Getter;
import lombok.Setter;

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

}

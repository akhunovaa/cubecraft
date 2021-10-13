package ru.mycubecraft.world.player;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Player {

    /**
     * The total height of the player's collision box.
     */
    public static final float PLAYER_HEIGHT = 1.80f;
    /**
     * The eye level of the player.
     */
    public static final float PLAYER_EYE_HEIGHT = 1.1f;
    /**
     * The width of the player's collision box.
     */
    public static final float PLAYER_WIDTH = 1.1f;

    protected boolean fly;
    protected boolean jumping;

}

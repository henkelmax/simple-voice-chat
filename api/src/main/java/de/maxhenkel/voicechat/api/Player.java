package de.maxhenkel.voicechat.api;

public interface Player extends Entity {

    /**
     * @return the actual player object
     */
    Object getPlayer();

    /**
     * @return the player name
     */
    String getName();

}

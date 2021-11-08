package de.maxhenkel.voicechat.api;

public interface ServerPlayer extends Player {

    /**
     * @return the level of the player
     */
    ServerLevel getServerLevel();

}

package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

public interface ServerPlayer extends Player {

    /**
     * @return the player the users camera is using
     */
    @Nullable
    ServerPlayer getCameraPlayer();

    /**
     * @return the level of the player
     */
    ServerLevel getServerLevel();

}

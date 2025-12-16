package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

public interface ServerPlayer extends Player {

    /**
     * @return the player the users camera is using
     */
    @Nullable
    ServerPlayer getCameraPlayer();

}

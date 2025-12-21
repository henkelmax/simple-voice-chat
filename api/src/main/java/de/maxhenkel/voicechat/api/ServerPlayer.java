package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

public interface ServerPlayer extends Player {

    /**
     * @return the player's name
     */
    String getName();

    /**
     * @return the player the user's camera is using
     */
    @Nullable
    ServerPlayer getCameraPlayer();

    /**
     * @return whether the player is in spectator mode
     */
    boolean isSpectator();

    /**
     * @param operatorUserPermissionLevel the permission level to check against
     * @return whether the player has the permissions
     */
    boolean hasPermissions(int operatorUserPermissionLevel);
}

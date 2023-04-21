package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

public interface VoicechatConnection {

    /**
     * @return the group of the player - <code>null</code> if the player is not in a group
     */
    @Nullable
    Group getGroup();

    /**
     * @return if the player is in a group
     */
    boolean isInGroup();

    /**
     * Joins this player to the provided group.
     *
     * @param group the group to join or <code>null</code> to leave the current group
     */
    void setGroup(@Nullable Group group);

    /**
     * This might not represent the actual state of the voice chat connection.
     *
     * @return if the player is connected to voice chat
     */
    boolean isConnected();

    /**
     * The players disconnected state will reset if its actual disconnected state changes or if the player reconnects.
     *
     * <b>NOTE</b>: This method will only work for players that don't have the mod installed. See {@link #isInstalled()}.
     *
     * @param connected if the player should be shown as connected to voice chat
     */
    void setConnected(boolean connected);

    /**
     * @return If the player muted the sound
     */
    boolean isDisabled();

    /**
     * Sets the players disabled state.
     * <b>NOTE</b>: This method will only work for players that don't have the mod installed. See {@link #isInstalled()}.
     *
     * @param disabled if the player should have the voice chat disabled icon
     */
    void setDisabled(boolean disabled);

    /**
     * @return if the player has a version of voice chat installed that is compatible with the server
     */
    boolean isInstalled();

    /**
     * @return the player
     */
    ServerPlayer getPlayer();

}

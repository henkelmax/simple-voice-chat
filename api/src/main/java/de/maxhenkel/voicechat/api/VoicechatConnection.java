package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

/**
 * <b>Note</b>: Connection objects must not be kept longer than the duration of the event they were obtained in.
 * Using them afterwards results in undefined behavior.
 * Re-fetch the connection using {@link VoicechatServerApi#getConnectionOf(java.util.UUID)} instead.
 */
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
     * This might not represent the actual state of the voice chat connection, since other voice chat plugins can fake having a player connected.
     *
     * @return if the player is connected to voice chat
     */
    boolean isConnected();

    /**
     * The players disconnected state will reset if its actual disconnected state changes or if the player reconnects.
     * <br/>
     * <b>NOTE</b>: This method will only work for players that don't have the mod installed. See {@link #isInstalled()}.
     *
     * @param connected if the player should be shown as connected to voice chat
     */
    void setConnected(boolean connected);

    /**
     * @return if the player has muted their microphone or has been force muted by the server
     */
    boolean isMuted();

    /**
     * Sets the players muted state.
     * <b>NOTE</b>: This method will only work for players that don't have the mod installed. See {@link #isInstalled()}.
     *
     * @param muted if the player should have the microphone muted icon
     */
    void setMuted(boolean muted);

    /**
     * See {@link de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent}.
     *
     * @return if the microphone of the player has been force muted by the server
     */
    boolean isForceMuted();

    /**
     * @return If the player muted the sound or has been force disabled by the server
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
     * See {@link de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent}.
     *
     * @return if the players voice chat has been force disabled by the server
     */
    boolean isForceDisabled();

    /**
     * Re-evaluates if the player is force muted or force disabled and emits a {@link de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent}.
     * <br/>
     * Call this when the conditions your addon uses to force mute or force disable this player change.
     *
     * @throws IllegalStateException if called from within a {@link de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent}
     */
    void evaluateForcedState();

    /**
     * @return if the player has a version of voice chat installed that is compatible with the server
     */
    boolean isInstalled();

    /**
     * @return the player
     */
    ServerPlayer getPlayer();

}

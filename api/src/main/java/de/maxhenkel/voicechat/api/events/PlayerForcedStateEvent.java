package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;

/**
 * This event is emitted when voice chat evaluates if a player is force muted or force disabled.
 * <br>
 * The initial values are based on the <code>voicechat.speak</code> and <code>voicechat.listen</code> permissions.
 * <br>
 * <b>NOTE</b>:
 * <br>
 * This event is emitted initially for a player after the {@link PlayerConnectedEvent} and then emitted periodically with the keep alive interval.
 * <br>
 * This is emitted off-thread and needs to return very quickly, don't run expensive code here!
 */
public interface PlayerForcedStateEvent extends ServerEvent {

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

    /**
     * @return if the player is force muted - either because of a missing <code>voicechat.speak</code> permission or because {@link #forceMute()} was called by this addon or an addon that received this event before this one
     */
    boolean isForceMuted();

    /**
     * @return if the player is force disabled - either because of a missing <code>voicechat.listen</code> permission or because {@link #forceDisable()} was called by this addon or an addon that received this event before this one
     */
    boolean isForceDisabled();

    /**
     * Force mutes the microphone of the player.
     * The server won't process any audio of the player and the player can't unmute their microphone.
     */
    void forceMute();

    /**
     * Force disables voice chat for the player.
     * The player won't receive any voice chat audio and can't enable voice chat.
     */
    void forceDisable();

}

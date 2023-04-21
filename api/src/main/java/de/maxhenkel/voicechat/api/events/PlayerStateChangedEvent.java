package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Called when a player changes his state.
 * For example if voice chat connects/disconnects, the player joins/leaves a group or disables voice chat.
 */
public interface PlayerStateChangedEvent extends ServerEvent {

    /**
     * @return if the player disabled voice chat
     */
    boolean isDisabled();

    /**
     * @return if the player is disconnected from voice chat
     */
    boolean isDisconnected();

    /**
     * @return the uuid of the player
     */
    UUID getPlayerUuid();

    /**
     * @return the connection of the player
     */
    @Nullable
    VoicechatConnection getConnection();

}

package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;

public interface PlayerConnectedEvent extends ServerEvent {

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}

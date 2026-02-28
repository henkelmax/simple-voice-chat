package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.ServerPlayer;

public interface VoiceHostEvent extends ServerEvent {

    /**
     * @return the voice host string
     */
    String getVoiceHost();

    /**
     * Overwrites voice chats voice host - This is sent to the client and used by it to connect to the server.
     *
     * @param voiceHost the voice host string
     */
    void setVoiceHost(String voiceHost);

    /**
     * @return the player that is connecting
     */
    ServerPlayer getPlayer();

}

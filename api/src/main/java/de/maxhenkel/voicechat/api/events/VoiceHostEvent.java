package de.maxhenkel.voicechat.api.events;

public interface VoiceHostEvent extends ServerEvent {

    /**
     * @return the voice host string
     */
    String getVoiceHost();

    /**
     * Overwrites voicechats voice host - This is sent to the client and used by it to connect to the server
     *
     * @param voiceHost the voice host string
     */
    void setVoiceHost(String voiceHost);

}

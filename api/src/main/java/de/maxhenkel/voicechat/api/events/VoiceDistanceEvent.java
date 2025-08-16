package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

/**
 * This event is emitted after a microphone packet arrives at the server and the distance is processed.
 * This can be used to modify the distance at which other players can hear this packet.
 */
public interface VoiceDistanceEvent extends ServerEvent {

    /**
     * @return the microphone packet
     */
    MicrophonePacket getPacket();

    /**
     * @return the connection of the player
     */
    VoicechatConnection getSenderConnection();

    /**
     * This returns either the default distance that's set on the server or the modified distance if another subscriber changed it already.
     * In case a player is whispering, this will return the whisper distance.
     * The whisper state can be obtained by calling {@link #getPacket()} and getting {@link MicrophonePacket#isWhispering()}.
     *
     * @return the distance in blocks
     */
    float getDistance();

    /**
     * Sets the distance this player sends audio at.
     * Note that this also includes the whisper distance, if the player is whispering.
     *
     * @param distance the distance in blocks
     */
    void setDistance(float distance);

}

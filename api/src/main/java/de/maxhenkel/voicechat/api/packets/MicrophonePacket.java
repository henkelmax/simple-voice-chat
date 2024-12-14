package de.maxhenkel.voicechat.api.packets;

public interface MicrophonePacket extends Packet, ConvertablePacket {

    /**
     * @return if the player is whispering
     */
    boolean isWhispering();

    /**
     * @return the opus encoded audio data from the player
     */
    byte[] getOpusEncodedData();

    /**
     * Allows you to modify or replace the opus encoded audio data.
     *
     * @param data the opus encoded audio data to replace
     */
    void setOpusEncodedData(byte[] data);

}

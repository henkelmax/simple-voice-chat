package de.maxhenkel.voicechat.api.packets;

import java.util.UUID;

public interface SoundPacket extends Packet, ConvertablePacket {

    /**
     * @return the sender of this packet - doesn't necessarily need to be a players UUID
     */
    UUID getSender();

    /**
     * @return the opus encoded audio data
     */
    byte[] getOpusEncodedData();

    /**
     * @return the sequence number of the packet
     */
    long getSequenceNumber();

}

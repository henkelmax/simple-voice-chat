package de.maxhenkel.voicechat.voice.common;

import java.util.UUID;

public abstract class SoundPacket<T extends SoundPacket> implements Packet<T> {

    protected UUID sender;
    protected byte[] data;
    protected long sequenceNumber;

    public SoundPacket(UUID sender, byte[] data, long sequenceNumber) {
        this.sender = sender;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
    }

    public SoundPacket(UUID sender, short[] data) {
        this.sender = sender;
        this.data = Utils.shortsToBytes(data);
        this.sequenceNumber = -1L;
    }

    public SoundPacket() {

    }

    public byte[] getData() {
        return data;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean isFromClientAudioChannel() {
        return sequenceNumber < 0L;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

}

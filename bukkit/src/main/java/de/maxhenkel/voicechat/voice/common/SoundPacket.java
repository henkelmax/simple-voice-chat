package de.maxhenkel.voicechat.voice.common;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class SoundPacket<T extends SoundPacket> implements Packet<T> {

    public static final byte WHISPER_MASK = 0b1;
    public static final byte HAS_CATEGORY_MASK = 0b10;

    protected UUID sender;
    protected byte[] data;
    protected long sequenceNumber;
    @Nullable
    protected String category;

    public SoundPacket(UUID sender, byte[] data, long sequenceNumber, @Nullable String category) {
        this.sender = sender;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.category = category;
    }

    public SoundPacket() {

    }

    public byte[] getData() {
        return data;
    }

    public UUID getSender() {
        return sender;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    protected boolean hasFlag(byte data, byte mask) {
        return (data & mask) != 0b0;
    }

    protected byte setFlag(byte data, byte mask) {
        return (byte) (data | mask);
    }

}

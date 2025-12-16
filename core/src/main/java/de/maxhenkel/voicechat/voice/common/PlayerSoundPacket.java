package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerSoundPacket extends SoundPacket<PlayerSoundPacket> {

    protected boolean whispering;
    protected float distance;

    public PlayerSoundPacket(UUID channelId, UUID sender, byte[] data, long sequenceNumber, boolean whispering, float distance, @Nullable String category) {
        super(channelId, sender, data, sequenceNumber, category);
        this.whispering = whispering;
        this.distance = distance;
    }

    public PlayerSoundPacket(UUID channelId, UUID sender, short[] data, boolean whispering, float distance, @Nullable String category) {
        super(channelId, sender, data, category);
        this.whispering = whispering;
        this.distance = distance;
    }

    public PlayerSoundPacket() {

    }

    public UUID getSender() {
        return sender;
    }

    public boolean isWhispering() {
        return whispering;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public PlayerSoundPacket fromBytes(ByteBuf buf) {
        PlayerSoundPacket soundPacket = new PlayerSoundPacket();
        soundPacket.channelId = BufferUtils.readUUID(buf);
        soundPacket.sender = BufferUtils.readUUID(buf);
        soundPacket.data = BufferUtils.readByteArray(buf);
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.distance = buf.readFloat();

        byte data = buf.readByte();
        soundPacket.whispering = hasFlag(data, WHISPER_MASK);
        if (hasFlag(data, HAS_CATEGORY_MASK)) {
            soundPacket.category = BufferUtils.readUtf(buf, 16);
        }
        return soundPacket;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, channelId);
        BufferUtils.writeUUID(buf, sender);
        BufferUtils.writeByteArray(buf, data);
        buf.writeLong(sequenceNumber);
        buf.writeFloat(distance);

        byte data = 0b0;
        if (whispering) {
            data = setFlag(data, WHISPER_MASK);
        }
        if (category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (category != null) {
            BufferUtils.writeUtf(buf, category, 16);
        }
    }

}

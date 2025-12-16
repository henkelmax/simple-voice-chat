package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class GroupSoundPacket extends SoundPacket<GroupSoundPacket> {

    public GroupSoundPacket(UUID channelId, UUID sender, byte[] data, long sequenceNumber, @Nullable String category) {
        super(channelId, sender, data, sequenceNumber, category);
    }

    public GroupSoundPacket(UUID channelId, UUID sender, short[] data, @Nullable String category) {
        super(channelId, sender, data, category);
    }

    public GroupSoundPacket() {

    }

    @Override
    public GroupSoundPacket fromBytes(ByteBuf buf) {
        GroupSoundPacket soundPacket = new GroupSoundPacket();
        soundPacket.channelId = BufferUtils.readUUID(buf);
        soundPacket.sender = BufferUtils.readUUID(buf);
        soundPacket.data = BufferUtils.readByteArray(buf);
        soundPacket.sequenceNumber = buf.readLong();

        byte data = buf.readByte();
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

        byte data = 0b0;
        if (category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (category != null) {
            BufferUtils.writeUtf(buf, category, 16);
        }
    }

}

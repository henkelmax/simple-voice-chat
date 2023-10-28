package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

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
    public GroupSoundPacket fromBytes(FriendlyByteBuf buf) {
        GroupSoundPacket soundPacket = new GroupSoundPacket();
        soundPacket.channelId = buf.readUUID();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();

        byte data = buf.readByte();
        if (hasFlag(data, HAS_CATEGORY_MASK)) {
            soundPacket.category = buf.readUtf(16);
        }
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(channelId);
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);

        byte data = 0b0;
        if (category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (category != null) {
            buf.writeUtf(category, 16);
        }
    }

}

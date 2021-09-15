package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class PlayerSoundPacket extends SoundPacket<PlayerSoundPacket> {

    public PlayerSoundPacket(UUID sender, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
    }

    public PlayerSoundPacket() {

    }

    public UUID getSender() {
        return sender;
    }

    @Override
    public PlayerSoundPacket fromBytes(FriendlyByteBuf buf) {
        PlayerSoundPacket soundPacket = new PlayerSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }

}

package de.maxhenkel.voicechat.voice.common;


import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class SoundPacket implements Packet<SoundPacket> {

    private UUID sender;
    private byte[] data;

    public SoundPacket(UUID sender, byte[] data) {
        this.sender = sender;
        this.data = data;
    }

    public SoundPacket() {

    }

    public byte[] getData() {
        return data;
    }

    public UUID getSender() {
        return sender;
    }

    @Override
    public SoundPacket fromBytes(PacketByteBuf buf) {
        SoundPacket soundPacket = new SoundPacket();
        soundPacket.sender = buf.readUuid();
        soundPacket.data = buf.readByteArray();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(sender);
        buf.writeByteArray(data);
    }
}

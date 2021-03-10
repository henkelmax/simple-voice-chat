package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

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
    public SoundPacket fromBytes(PacketBuffer buf) {
        SoundPacket soundPacket = new SoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
    }
}

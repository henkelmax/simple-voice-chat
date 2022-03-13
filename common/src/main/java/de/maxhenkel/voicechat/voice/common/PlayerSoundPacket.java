package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class PlayerSoundPacket extends SoundPacket<PlayerSoundPacket> {

    protected boolean whispering;

    public PlayerSoundPacket(UUID sender, byte[] data, long sequenceNumber, boolean whispering) {
        super(sender, data, sequenceNumber);
        this.whispering = whispering;
    }

    public PlayerSoundPacket(UUID sender, short[] data, boolean whispering) {
        super(sender, data);
        this.whispering = whispering;
    }

    public PlayerSoundPacket() {

    }

    public UUID getSender() {
        return sender;
    }

    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public PlayerSoundPacket fromBytes(PacketBuffer buf) {
        PlayerSoundPacket soundPacket = new PlayerSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.whispering = buf.readBoolean();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
        buf.writeBoolean(whispering);
    }

}

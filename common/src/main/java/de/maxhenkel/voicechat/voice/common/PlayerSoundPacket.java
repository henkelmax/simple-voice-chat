package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class PlayerSoundPacket extends SoundPacket<PlayerSoundPacket> {

    protected boolean whispering;
    protected float distance;

    public PlayerSoundPacket(UUID sender, byte[] data, long sequenceNumber, boolean whispering, float distance) {
        super(sender, data, sequenceNumber);
        this.whispering = whispering;
        this.distance = distance;
    }

    public PlayerSoundPacket(UUID sender, short[] data, boolean whispering, float distance) {
        super(sender, data);
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
    public PlayerSoundPacket fromBytes(FriendlyByteBuf buf) {
        PlayerSoundPacket soundPacket = new PlayerSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.whispering = buf.readBoolean();
        soundPacket.distance = buf.readFloat();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
        buf.writeBoolean(whispering);
        buf.writeFloat(distance);
    }

}

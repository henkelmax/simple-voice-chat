package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    private UUID playerUUID;
    private byte[] nonce;

    public ConnectionCheckPacket(UUID playerUUID, byte[] nonce) {
        this.playerUUID = playerUUID;
        this.nonce = nonce;
    }

    public ConnectionCheckPacket() {

    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public ConnectionCheckPacket fromBytes(FriendlyByteBuf buf) {
        ConnectionCheckPacket packet = new ConnectionCheckPacket();
        packet.playerUUID = buf.readUUID();
        packet.nonce = new byte[Secret.NONCE_SIZE_BYTES];
        buf.readBytes(packet.nonce);
        return packet;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeBytes(nonce);
    }
}

package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class AuthenticatePacket implements Packet<AuthenticatePacket> {

    private UUID playerUUID;
    private UUID secret;

    public AuthenticatePacket(UUID playerUUID, UUID secret) {
        this.playerUUID = playerUUID;
        this.secret = secret;
    }

    public AuthenticatePacket() {

    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }

    @Override
    public AuthenticatePacket fromBytes(PacketBuffer buf) {
        AuthenticatePacket packet = new AuthenticatePacket();
        packet.playerUUID = buf.readUUID();
        packet.secret = buf.readUUID();
        return packet;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeUUID(secret);
    }
}

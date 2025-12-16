package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class AuthenticatePacket implements Packet<AuthenticatePacket> {

    private UUID playerUUID;
    private Secret secret;

    public AuthenticatePacket(UUID playerUUID, Secret secret) {
        this.playerUUID = playerUUID;
        this.secret = secret;
    }

    public AuthenticatePacket() {

    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Secret getSecret() {
        return secret;
    }

    @Override
    public AuthenticatePacket fromBytes(ByteBuf buf) {
        AuthenticatePacket packet = new AuthenticatePacket();
        packet.playerUUID = BufferUtils.readUUID(buf);
        packet.secret = Secret.fromBytes(buf);
        return packet;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, playerUUID);
        secret.toBytes(buf);
    }
}

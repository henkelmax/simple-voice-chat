package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

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
    public AuthenticatePacket fromBytes(FriendlyByteBuf buf) {
        AuthenticatePacket packet = new AuthenticatePacket();
        packet.playerUUID = buf.readUUID();
        packet.secret = Secret.fromBytes(buf);
        return packet;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        secret.toBytes(buf);
    }
}

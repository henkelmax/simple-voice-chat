package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class AuthenticationMessage implements Message<AuthenticationMessage> {

    private UUID playerUUID;
    private UUID secret;

    public AuthenticationMessage(UUID playerUUID, UUID secret) {
        this.playerUUID = playerUUID;
        this.secret = secret;
    }

    public AuthenticationMessage() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        Main.CLIENT_VOICE_EVENTS.authenticate(playerUUID, secret);
    }

    @Override
    public AuthenticationMessage fromBytes(PacketBuffer buf) {
        playerUUID = buf.readUUID();
        secret = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeUUID(secret);
    }
}

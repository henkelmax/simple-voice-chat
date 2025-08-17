package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class RemovePlayerStatePacket implements Packet<RemovePlayerStatePacket> {

    public static final ResourceLocation REMOVE_PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "remove_state");

    private UUID id;

    public RemovePlayerStatePacket() {

    }

    public RemovePlayerStatePacket(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return REMOVE_PLAYER_STATE;
    }

    @Override
    public RemovePlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        id = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
    }

}

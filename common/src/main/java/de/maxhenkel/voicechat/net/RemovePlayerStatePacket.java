package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class RemovePlayerStatePacket implements Packet<RemovePlayerStatePacket> {

    public static final Type<RemovePlayerStatePacket> REMOVE_PLAYER_STATE = new Type<>(Identifier.fromNamespaceAndPath(Voicechat.MODID, "remove_state"));

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
    public RemovePlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        id = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
    }

    @Override
    public Type<RemovePlayerStatePacket> type() {
        return REMOVE_PLAYER_STATE;
    }

}

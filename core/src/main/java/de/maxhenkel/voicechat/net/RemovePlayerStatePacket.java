package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

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
    public RemovePlayerStatePacket fromBytes(ByteBuf buf) {
        id = BufferUtils.readUUID(buf);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, id);
    }

}

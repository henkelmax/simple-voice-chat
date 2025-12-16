package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class RemovePlayerStatePacket implements Packet<RemovePlayerStatePacket> {

    public static final String REMOVE_PLAYER_STATE = "remove_state";

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
    public String getID() {
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

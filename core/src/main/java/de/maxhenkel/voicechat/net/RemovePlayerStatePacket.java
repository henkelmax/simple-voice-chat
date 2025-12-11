package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;

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
    public RemovePlayerStatePacket fromBytes(VCByteBuf buf) {
        id = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeUUID(id);
    }

}

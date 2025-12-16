package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final String PLAYER_STATE = "update_state";

    private boolean disabled;

    public UpdateStatePacket() {

    }

    public UpdateStatePacket(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String getIdentifier() {
        return PLAYER_STATE;
    }

    @Override
    public UpdateStatePacket fromBytes(ByteBuf buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(disabled);
    }

}

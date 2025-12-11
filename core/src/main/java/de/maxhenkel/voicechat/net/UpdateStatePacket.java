package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.Packet;
import de.maxhenkel.voicechat.api.VCByteBuf;

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
    public String getID() {
        return PLAYER_STATE;
    }

    @Override
    public UpdateStatePacket fromBytes(VCByteBuf buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeBoolean(disabled);
    }

}

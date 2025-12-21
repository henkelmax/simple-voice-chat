package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "update_state");

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
    public ResourceLocation getIdentifier() {
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

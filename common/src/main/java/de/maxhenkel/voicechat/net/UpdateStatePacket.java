package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(NetManager.CHANNEL, "update_state");

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
    public UpdateStatePacket fromBytes(PacketBuffer buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(disabled);
    }

}

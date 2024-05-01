package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final CustomPacketPayload.Type<UpdateStatePacket> PLAYER_STATE = new CustomPacketPayload.Type<>(new ResourceLocation(Voicechat.MODID, "update_state"));

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
    public UpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disabled);
    }

    @Override
    public Type<UpdateStatePacket> type() {
        return PLAYER_STATE;
    }

}

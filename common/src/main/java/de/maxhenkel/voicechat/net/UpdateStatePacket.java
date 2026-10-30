package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final CustomPacketPayload.Type<UpdateStatePacket> PLAYER_STATE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Voicechat.MODID, "client_state"));

    private boolean muted;
    private boolean disabled;

    public UpdateStatePacket() {

    }

    public UpdateStatePacket(boolean muted, boolean disabled) {
        this.muted = muted;
        this.disabled = disabled;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public UpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        muted = buf.readBoolean();
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(muted);
        buf.writeBoolean(disabled);
    }

    @Override
    public Type<UpdateStatePacket> type() {
        return PLAYER_STATE;
    }

}

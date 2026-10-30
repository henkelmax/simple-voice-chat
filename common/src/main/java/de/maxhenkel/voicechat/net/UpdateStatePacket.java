package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "update_state");

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
    public ResourceLocation getIdentifier() {
        return PLAYER_STATE;
    }

    @Override
    public UpdateStatePacket fromBytes(PacketBuffer buf) {
        muted = buf.readBoolean();
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(muted);
        buf.writeBoolean(disabled);
    }

}

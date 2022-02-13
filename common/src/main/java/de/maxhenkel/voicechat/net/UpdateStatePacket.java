package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "update_state");

    private boolean disconnected;
    private boolean disabled;

    public UpdateStatePacket() {

    }

    public UpdateStatePacket(boolean disconnected, boolean disabled) {
        this.disconnected = disconnected;
        this.disabled = disabled;
    }

    public boolean isDisconnected() {
        return disconnected;
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
        disconnected = buf.readBoolean();
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(disconnected);
        buf.writeBoolean(disabled);
    }

}

package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class LegacyUpdateStatePacket implements Packet<LegacyUpdateStatePacket> {

    public static final ResourceLocation UPDATE_STATE = new ResourceLocation(Voicechat.MODID, "update_state");

    private boolean disabled;

    public LegacyUpdateStatePacket() {

    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public LegacyUpdateStatePacket fromBytes(PacketBuffer buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(disabled);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return UPDATE_STATE;
    }

}

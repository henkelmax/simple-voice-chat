package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class LegacyUpdateStatePacket implements Packet<LegacyUpdateStatePacket> {

    public static final CustomPacketPayload.Type<LegacyUpdateStatePacket> UPDATE_STATE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "update_state"));

    private boolean disabled;

    public LegacyUpdateStatePacket() {

    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public LegacyUpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disabled);
    }

    @Override
    public Type<LegacyUpdateStatePacket> type() {
        return UPDATE_STATE;
    }

}

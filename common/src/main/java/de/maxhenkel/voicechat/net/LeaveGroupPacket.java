package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final CustomPacketPayload.Type<LeaveGroupPacket> LEAVE_GROUP = new CustomPacketPayload.Type<>(new ResourceLocation(Voicechat.MODID, "leave_group"));

    public LeaveGroupPacket() {

    }

    @Override
    public LeaveGroupPacket fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public Type<LeaveGroupPacket> type() {
        return LEAVE_GROUP;
    }

}

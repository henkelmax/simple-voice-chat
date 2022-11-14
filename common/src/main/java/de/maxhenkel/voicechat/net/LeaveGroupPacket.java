package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final ResourceLocation LEAVE_GROUP = new ResourceLocation(NetManager.CHANNEL, "leave_group");

    public LeaveGroupPacket() {

    }

    @Override
    public ResourceLocation getIdentifier() {
        return LEAVE_GROUP;
    }

    @Override
    public LeaveGroupPacket fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }

}

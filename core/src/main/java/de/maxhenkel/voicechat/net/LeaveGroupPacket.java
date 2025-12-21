package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final ResourceLocation LEAVE_GROUP = new ResourceLocation(Voicechat.MODID, "leave_group");

    public LeaveGroupPacket() {

    }

    @Override
    public ResourceLocation getIdentifier() {
        return LEAVE_GROUP;
    }

    @Override
    public LeaveGroupPacket fromBytes(ByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

}

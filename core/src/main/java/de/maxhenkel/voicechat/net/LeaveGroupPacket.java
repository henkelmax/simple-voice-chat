package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final String LEAVE_GROUP = "leave_group";

    public LeaveGroupPacket() {

    }

    @Override
    public String getID() {
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

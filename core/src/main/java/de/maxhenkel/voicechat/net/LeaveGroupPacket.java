package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final String LEAVE_GROUP = "leave_group";

    public LeaveGroupPacket() {

    }

    @Override
    public String getID() {
        return LEAVE_GROUP;
    }

    @Override
    public LeaveGroupPacket fromBytes(VCByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {

    }

}

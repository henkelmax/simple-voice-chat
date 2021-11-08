package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import com.comphenix.protocol.wrappers.MinecraftKey;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final MinecraftKey LEAVE_GROUP = new MinecraftKey(Voicechat.MODID, "leave_group");

    public LeaveGroupPacket() {

    }

    @Override
    public MinecraftKey getID() {
        return LEAVE_GROUP;
    }

    @Override
    public LeaveGroupPacket fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

}

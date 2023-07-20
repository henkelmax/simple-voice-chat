package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final Key LEAVE_GROUP = Voicechat.compatibility.createNamespacedKey("leave_group");

    public LeaveGroupPacket() {

    }

    @Override
    public Key getID() {
        return LEAVE_GROUP;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getGroupManager().onLeaveGroupPacket(player, this);
    }

    @Override
    public LeaveGroupPacket fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

}

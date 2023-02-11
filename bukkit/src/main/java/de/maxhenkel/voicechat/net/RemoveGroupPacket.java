package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class RemoveGroupPacket implements Packet<RemoveGroupPacket> {

    public static final NamespacedKey REMOVE_GROUP = new NamespacedKey(Voicechat.MODID, "remove_group");

    private UUID groupId;

    public RemoveGroupPacket() {

    }

    public RemoveGroupPacket(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    @Override
    public NamespacedKey getID() {
        return REMOVE_GROUP;
    }

    @Override
    public RemoveGroupPacket fromBytes(FriendlyByteBuf buf) {
        groupId = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(groupId);
    }

}

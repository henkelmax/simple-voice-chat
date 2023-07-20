package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public class RemoveGroupPacket implements Packet<RemoveGroupPacket> {

    public static final Key REMOVE_GROUP = Voicechat.compatibility.createNamespacedKey("remove_group");

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
    public Key getID() {
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

package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RemoveGroupPacket implements Packet<RemoveGroupPacket> {

    public static final ResourceLocation REMOVE_GROUP = new ResourceLocation(NetManager.CHANNEL, "remove_group");

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
    public ResourceLocation getIdentifier() {
        return REMOVE_GROUP;
    }

    @Override
    public RemoveGroupPacket fromBytes(PacketBuffer buf) {
        groupId = buf.readUniqueId();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(groupId);
    }

}

package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RemoveGroupPacket implements Packet<RemoveGroupPacket> {

    public static final ResourceLocation REMOVE_GROUP = new ResourceLocation(Voicechat.MODID, "remove_group");

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
        groupId = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(groupId);
    }

}

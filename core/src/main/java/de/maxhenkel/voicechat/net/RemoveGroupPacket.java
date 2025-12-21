package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

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
    public RemoveGroupPacket fromBytes(ByteBuf buf) {
        groupId = BufferUtils.readUUID(buf);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, groupId);
    }

}

package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class RemoveGroupPacket implements Packet<RemoveGroupPacket> {

    public static final String REMOVE_GROUP = "remove_group";

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
    public String getID() {
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

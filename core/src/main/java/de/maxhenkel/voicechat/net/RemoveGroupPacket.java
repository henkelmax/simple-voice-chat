package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.Packet;
import de.maxhenkel.voicechat.api.VCByteBuf;

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
    public RemoveGroupPacket fromBytes(VCByteBuf buf) {
        groupId = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeUUID(groupId);
    }

}

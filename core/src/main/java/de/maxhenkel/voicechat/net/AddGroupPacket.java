package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class AddGroupPacket implements Packet<AddGroupPacket> {

    public static final ResourceLocation ADD_ADD_GROUP = new ResourceLocation(Voicechat.MODID, "add_group");

    private ClientGroup group;

    public AddGroupPacket() {

    }

    public AddGroupPacket(ClientGroup group) {
        this.group = group;
    }

    public ClientGroup getGroup() {
        return group;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return ADD_ADD_GROUP;
    }

    @Override
    public AddGroupPacket fromBytes(ByteBuf buf) {
        group = ClientGroup.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        group.toBytes(buf);
    }

}

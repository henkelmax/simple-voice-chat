package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

public class AddGroupPacket implements Packet<AddGroupPacket> {

    public static final String ADD_ADD_GROUP = "add_group";

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
    public String getID() {
        return ADD_ADD_GROUP;
    }

    @Override
    public AddGroupPacket fromBytes(VCByteBuf buf) {
        group = ClientGroup.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        group.toBytes(buf);
    }

}

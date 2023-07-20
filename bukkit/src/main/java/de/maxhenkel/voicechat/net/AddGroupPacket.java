package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.kyori.adventure.key.Key;

public class AddGroupPacket implements Packet<AddGroupPacket> {

    public static final Key ADD_ADD_GROUP = Voicechat.compatibility.createNamespacedKey("add_group");

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
    public Key getID() {
        return ADD_ADD_GROUP;
    }

    @Override
    public AddGroupPacket fromBytes(FriendlyByteBuf buf) {
        group = ClientGroup.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        group.toBytes(buf);
    }

}

package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import org.bukkit.NamespacedKey;

public class AddGroupPacket implements Packet<AddGroupPacket> {

    public static final NamespacedKey ADD_ADD_GROUP = new NamespacedKey(Voicechat.MODID, "add_group");

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
    public NamespacedKey getID() {
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

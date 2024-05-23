package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class AddGroupPacket implements Packet<AddGroupPacket> {

    public static final CustomPacketPayload.Type<AddGroupPacket> ADD_ADD_GROUP = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "add_group"));

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
    public AddGroupPacket fromBytes(FriendlyByteBuf buf) {
        group = ClientGroup.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        group.toBytes(buf);
    }

    @Override
    public Type<AddGroupPacket> type() {
        return ADD_ADD_GROUP;
    }

}

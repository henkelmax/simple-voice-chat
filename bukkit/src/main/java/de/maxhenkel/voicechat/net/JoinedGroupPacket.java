package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.ResourceLocation;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

import javax.annotation.Nullable;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final ResourceLocation JOINED_GROUP = new ResourceLocation(Voicechat.MODID, "joined_group");

    @Nullable
    private ClientGroup group;

    public JoinedGroupPacket() {

    }

    public JoinedGroupPacket(@Nullable ClientGroup group) {
        this.group = group;
    }

    @Nullable
    public ClientGroup getGroup() {
        return group;
    }

    @Override
    public ResourceLocation getID() {
        return JOINED_GROUP;
    }

    @Override
    public JoinedGroupPacket fromBytes(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            group = ClientGroup.fromBytes(buf);
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            group.toBytes(buf);
        }
    }

}

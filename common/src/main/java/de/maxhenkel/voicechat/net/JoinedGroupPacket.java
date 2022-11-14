package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final ResourceLocation JOINED_GROUP = new ResourceLocation(NetManager.CHANNEL, "joined_group");

    @Nullable
    private ClientGroup group;
    private boolean wrongPassword;

    public JoinedGroupPacket() {

    }

    public JoinedGroupPacket(@Nullable ClientGroup group, boolean wrongPassword) {
        this.group = group;
        this.wrongPassword = wrongPassword;
    }

    @Nullable
    public ClientGroup getGroup() {
        return group;
    }

    public boolean isWrongPassword() {
        return wrongPassword;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return JOINED_GROUP;
    }

    @Override
    public JoinedGroupPacket fromBytes(PacketBuffer buf) {
        if (buf.readBoolean()) {
            group = ClientGroup.fromBytes(buf);
        }
        wrongPassword = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            group.toBytes(buf);
        }
        buf.writeBoolean(wrongPassword);
    }

}

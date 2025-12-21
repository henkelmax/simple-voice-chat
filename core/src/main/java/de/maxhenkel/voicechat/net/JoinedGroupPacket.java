package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final ResourceLocation JOINED_GROUP = new ResourceLocation(Voicechat.MODID, "joined_group");

    @Nullable
    private UUID group;
    private boolean wrongPassword;

    public JoinedGroupPacket() {

    }

    public JoinedGroupPacket(@Nullable UUID group, boolean wrongPassword) {
        this.group = group;
        this.wrongPassword = wrongPassword;
    }

    @Nullable
    public UUID getGroup() {
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
    public JoinedGroupPacket fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            group = BufferUtils.readUUID(buf);
        }
        wrongPassword = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            BufferUtils.writeUUID(buf, group);
        }
        buf.writeBoolean(wrongPassword);
    }

}

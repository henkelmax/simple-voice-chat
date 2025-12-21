package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinGroupPacket implements Packet<JoinGroupPacket> {

    public static final ResourceLocation SET_GROUP = new ResourceLocation(Voicechat.MODID, "set_group");

    private UUID group;
    @Nullable
    private String password;

    public JoinGroupPacket() {

    }

    public JoinGroupPacket(UUID group, @Nullable String password) {
        this.group = group;
        this.password = password;
    }

    public UUID getGroup() {
        return group;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return SET_GROUP;
    }

    @Override
    public JoinGroupPacket fromBytes(ByteBuf buf) {
        group = BufferUtils.readUUID(buf);
        if (buf.readBoolean()) {
            password = BufferUtils.readUtf(buf, 512);
        }
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, group);
        buf.writeBoolean(password != null);
        if (password != null) {
            BufferUtils.writeUtf(buf, password, 512);
        }
    }

}

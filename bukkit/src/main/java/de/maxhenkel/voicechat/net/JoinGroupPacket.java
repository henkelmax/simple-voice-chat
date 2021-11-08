package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinGroupPacket implements Packet<JoinGroupPacket> {

    public static final MinecraftKey SET_GROUP = new MinecraftKey(Voicechat.MODID, "set_group");

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
    public MinecraftKey getID() {
        return SET_GROUP;
    }

    @Override
    public JoinGroupPacket fromBytes(FriendlyByteBuf buf) {
        group = buf.readUUID();
        if (buf.readBoolean()) {
            password = buf.readUtf(512);
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(group);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeUtf(password, 512);
        }
    }

}

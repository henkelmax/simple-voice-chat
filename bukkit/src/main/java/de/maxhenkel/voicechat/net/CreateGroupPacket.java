package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final MinecraftKey CREATE_GROUP = new MinecraftKey(Voicechat.MODID, "create_group");

    private String name;
    @Nullable
    private String password;

    public CreateGroupPacket() {

    }

    public CreateGroupPacket(String name, @Nullable String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Override
    public MinecraftKey getID() {
        return CREATE_GROUP;
    }

    @Override
    public CreateGroupPacket fromBytes(FriendlyByteBuf buf) {
        name = buf.readUtf(512);
        password = null;
        if (buf.readBoolean()) {
            password = buf.readUtf(512);
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name, 512);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeUtf(password, 512);
        }
    }

}

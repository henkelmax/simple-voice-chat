package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final ResourceLocation CREATE_GROUP = new ResourceLocation(NetManager.CHANNEL, "create_group");

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
    public ResourceLocation getIdentifier() {
        return CREATE_GROUP;
    }

    @Override
    public CreateGroupPacket fromBytes(PacketBuffer buf) {
        name = buf.readString(512);
        password = null;
        if (buf.readBoolean()) {
            password = buf.readString(512);
        }
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeString(password);
        }
    }

}

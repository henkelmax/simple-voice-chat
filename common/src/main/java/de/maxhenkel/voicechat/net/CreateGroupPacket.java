package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final ResourceLocation CREATE_GROUP = new ResourceLocation(NetManager.CHANNEL, "create_group");

    private String name;
    @Nullable
    private String password;
    private Group.Type type;

    public CreateGroupPacket() {

    }

    public CreateGroupPacket(String name, @Nullable String password, Group.Type type) {
        this.name = name;
        this.password = password;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public Group.Type getType() {
        return type;
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
        type = GroupImpl.TypeImpl.fromInt(buf.readShort());
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeString(password);
        }
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

}

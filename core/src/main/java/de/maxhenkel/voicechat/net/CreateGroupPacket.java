package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VCByteBuf;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final String CREATE_GROUP = "create_group";

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
    public String getID() {
        return CREATE_GROUP;
    }

    @Override
    public CreateGroupPacket fromBytes(VCByteBuf buf) {
        name = buf.readUtf(512);
        password = null;
        if (buf.readBoolean()) {
            password = buf.readUtf(512);
        }
        type = GroupImpl.TypeImpl.fromInt(buf.readShort());
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeUtf(name, 512);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeUtf(password, 512);
        }
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

}

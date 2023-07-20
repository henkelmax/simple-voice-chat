package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final Key CREATE_GROUP = Voicechat.compatibility.createNamespacedKey("create_group");

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
    public Key getID() {
        return CREATE_GROUP;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getGroupManager().onCreateGroupPacket(player, this);
    }

    @Override
    public CreateGroupPacket fromBytes(FriendlyByteBuf buf) {
        name = buf.readUtf(512);
        password = null;
        if (buf.readBoolean()) {
            password = buf.readUtf(512);
        }
        type = GroupImpl.TypeImpl.fromInt(buf.readShort());
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name, 512);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeUtf(password, 512);
        }
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

}

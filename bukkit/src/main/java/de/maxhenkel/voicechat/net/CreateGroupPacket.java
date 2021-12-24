package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.NamespacedKeyUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class CreateGroupPacket implements Packet<CreateGroupPacket> {

    public static final NamespacedKey CREATE_GROUP = NamespacedKeyUtil.voicechat("create_group");

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
    public NamespacedKey getID() {
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

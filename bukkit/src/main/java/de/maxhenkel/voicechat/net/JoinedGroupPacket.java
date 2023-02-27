package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.NamespacedKeyUtil;
import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final NamespacedKey JOINED_GROUP = NamespacedKeyUtil.voicechat("joined_group");

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
    public NamespacedKey getID() {
        return JOINED_GROUP;
    }

    @Override
    public JoinedGroupPacket fromBytes(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            group = buf.readUUID();
        }
        wrongPassword = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            buf.writeUUID(group);
        }
        buf.writeBoolean(wrongPassword);
    }

}

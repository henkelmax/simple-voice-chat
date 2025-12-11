package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.Packet;
import de.maxhenkel.voicechat.api.VCByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final String JOINED_GROUP = "joined_group";

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
    public String getID() {
        return JOINED_GROUP;
    }

    @Override
    public JoinedGroupPacket fromBytes(VCByteBuf buf) {
        if (buf.readBoolean()) {
            group = buf.readUUID();
        }
        wrongPassword = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            buf.writeUUID(group);
        }
        buf.writeBoolean(wrongPassword);
    }

}

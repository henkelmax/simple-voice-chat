package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinGroupPacket implements Packet<JoinGroupPacket> {

    public static final CustomPacketPayload.Type<JoinGroupPacket> SET_GROUP = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "set_group"));

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
    public JoinGroupPacket fromBytes(FriendlyByteBuf buf) {
        group = buf.readUUID();
        if (buf.readBoolean()) {
            password = buf.readUtf(Voicechat.MAX_GROUP_PASSWORD_LENGTH);
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(group);
        buf.writeBoolean(password != null);
        if (password != null) {
            buf.writeUtf(password, Voicechat.MAX_GROUP_PASSWORD_LENGTH);
        }
    }

    @Override
    public Type<JoinGroupPacket> type() {
        return SET_GROUP;
    }

}

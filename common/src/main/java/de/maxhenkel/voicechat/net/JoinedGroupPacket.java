package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class JoinedGroupPacket implements Packet<JoinedGroupPacket> {

    public static final CustomPacketPayload.Type<JoinedGroupPacket> JOINED_GROUP = new CustomPacketPayload.Type<>(new ResourceLocation(Voicechat.MODID, "joined_group"));

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
    public JoinedGroupPacket fromBytes(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            group = buf.readUUID();
        }
        wrongPassword = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(group != null);
        if (group != null) {
            buf.writeUUID(group);
        }
        buf.writeBoolean(wrongPassword);
    }

    @Override
    public Type<JoinedGroupPacket> type() {
        return JOINED_GROUP;
    }

}

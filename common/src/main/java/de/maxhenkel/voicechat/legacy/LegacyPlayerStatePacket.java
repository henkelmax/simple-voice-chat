package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class LegacyPlayerStatePacket implements Packet<LegacyPlayerStatePacket> {

    public static final CustomPacketPayload.Type<LegacyPlayerStatePacket> PLAYER_STATE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "player_state"));

    private PlayerState playerState;

    public LegacyPlayerStatePacket() {

    }

    public LegacyPlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    @Override
    public LegacyPlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        LegacyProtocolV20.writePlayerState(buf, playerState);
    }

    @Override
    public Type<LegacyPlayerStatePacket> type() {
        return PLAYER_STATE;
    }

}

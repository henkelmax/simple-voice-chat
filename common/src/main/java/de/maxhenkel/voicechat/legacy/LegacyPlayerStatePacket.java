package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class LegacyPlayerStatePacket implements Packet<LegacyPlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(NetManager.CHANNEL, "player_state");

    private PlayerState playerState;

    public LegacyPlayerStatePacket() {

    }

    public LegacyPlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    @Override
    public LegacyPlayerStatePacket fromBytes(PacketBuffer buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        LegacyProtocolV20.writePlayerState(buf, playerState);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATE;
    }

}

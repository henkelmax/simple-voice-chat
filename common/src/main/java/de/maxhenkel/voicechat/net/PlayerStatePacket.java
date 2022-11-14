package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(NetManager.CHANNEL, "player_state");

    private PlayerState playerState;

    public PlayerStatePacket() {

    }

    public PlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATE;
    }

    @Override
    public PlayerStatePacket fromBytes(PacketBuffer buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        playerState.toBytes(buf);
    }

}

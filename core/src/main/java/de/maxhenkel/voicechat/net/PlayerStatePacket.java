package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "state");

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
    public PlayerStatePacket fromBytes(ByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        playerState.toBytes(buf);
    }

}

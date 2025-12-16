package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.PlayerState;
import io.netty.buffer.ByteBuf;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final String PLAYER_STATE = "state";

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
    public String getID() {
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

package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.Packet;
import de.maxhenkel.voicechat.api.VCByteBuf;
import de.maxhenkel.voicechat.voice.common.PlayerState;

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
    public PlayerStatePacket fromBytes(VCByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        playerState.toBytes(buf);
    }

}

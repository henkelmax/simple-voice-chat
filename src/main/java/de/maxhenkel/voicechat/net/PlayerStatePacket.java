package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final Identifier PLAYER_STATE = new Identifier(Voicechat.MODID, "player_state");

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
    public Identifier getID() {
        return PLAYER_STATE;
    }

    @Override
    public PlayerStatePacket fromBytes(PacketByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        playerState.toBytes(buf);
    }

}

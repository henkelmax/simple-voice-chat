package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

import java.util.*;

public class PlayerStatesPacket implements Packet<PlayerStatesPacket> {

    public static final ResourceLocation PLAYER_STATES = new ResourceLocation(Voicechat.MODID, "states");

    private Collection<PlayerState> playerStates;

    public PlayerStatesPacket() {

    }

    public PlayerStatesPacket(Collection<PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public Collection<PlayerState> getPlayerStates() {
        return playerStates;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATES;
    }

    @Override
    public PlayerStatesPacket fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        playerStates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            PlayerState playerState = PlayerState.fromBytes(buf);
            playerStates.add(playerState);
        }

        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (PlayerState state : playerStates) {
            state.toBytes(buf);
        }
    }

}

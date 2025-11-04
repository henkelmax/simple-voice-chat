package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.*;

public class PlayerStatesPacket implements Packet<PlayerStatesPacket> {

    public static final CustomPacketPayload.Type<PlayerStatesPacket> PLAYER_STATES = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Voicechat.MODID, "states"));

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
    public PlayerStatesPacket fromBytes(FriendlyByteBuf buf) {
        int count = buf.readInt();
        playerStates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            PlayerState playerState = PlayerState.fromBytes(buf);
            playerStates.add(playerState);
        }

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (PlayerState state : playerStates) {
            state.toBytes(buf);
        }
    }

    @Override
    public Type<PlayerStatesPacket> type() {
        return PLAYER_STATES;
    }

}

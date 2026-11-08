package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.Key;
import de.maxhenkel.voicechat.voice.common.PlayerState;

import java.util.Collection;

public class LegacyPlayerStatesPacket implements Packet<LegacyPlayerStatesPacket> {

    public static final Key PLAYER_STATES = Voicechat.compatibility.createNamespacedKey("player_states");

    private Collection<PlayerState> playerStates;

    public LegacyPlayerStatesPacket() {

    }

    public LegacyPlayerStatesPacket(Collection<PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    @Override
    public Key getID() {
        return PLAYER_STATES;
    }

    @Override
    public LegacyPlayerStatesPacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        LegacyProtocolV20.writePlayerStates(buf, playerStates);
    }

}

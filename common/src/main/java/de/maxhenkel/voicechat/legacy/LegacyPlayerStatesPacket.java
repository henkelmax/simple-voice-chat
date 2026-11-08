package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Collection;

public class LegacyPlayerStatesPacket implements Packet<LegacyPlayerStatesPacket> {

    public static final CustomPacketPayload.Type<LegacyPlayerStatesPacket> PLAYER_STATES = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Voicechat.MODID, "player_states"));

    private Collection<PlayerState> playerStates;

    public LegacyPlayerStatesPacket() {

    }

    public LegacyPlayerStatesPacket(Collection<PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    @Override
    public LegacyPlayerStatesPacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        LegacyProtocolV20.writePlayerStates(buf, playerStates);
    }

    @Override
    public Type<LegacyPlayerStatesPacket> type() {
        return PLAYER_STATES;
    }

}

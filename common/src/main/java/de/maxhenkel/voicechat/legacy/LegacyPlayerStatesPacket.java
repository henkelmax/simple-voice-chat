package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class LegacyPlayerStatesPacket implements Packet<LegacyPlayerStatesPacket> {

    public static final ResourceLocation PLAYER_STATES = new ResourceLocation(NetManager.CHANNEL, "player_states");

    private Collection<PlayerState> playerStates;

    public LegacyPlayerStatesPacket() {

    }

    public LegacyPlayerStatesPacket(Collection<PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    @Override
    public LegacyPlayerStatesPacket fromBytes(PacketBuffer buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        LegacyProtocolV20.writePlayerStates(buf, playerStates);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATES;
    }

}

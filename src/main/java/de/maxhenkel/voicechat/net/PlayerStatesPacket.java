package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatesPacket implements Packet<PlayerStatesPacket> {

    private Map<UUID, PlayerState> playerStates;

    public static final Identifier PLAYER_STATES = new Identifier(Voicechat.MODID, "player_states");

    public PlayerStatesPacket() {

    }

    public PlayerStatesPacket(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    @Override
    public Identifier getID() {
        return PLAYER_STATES;
    }

    @Override
    public PlayerStatesPacket fromBytes(PacketByteBuf buf) {
        playerStates = new HashMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            playerStates.put(buf.readUuid(), new PlayerState(buf.readBoolean(), buf.readBoolean()));
        }

        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
            buf.writeUuid(entry.getKey());
            buf.writeBoolean(entry.getValue().isDisabled());
            buf.writeBoolean(entry.getValue().isDisconnected());
        }
    }

}

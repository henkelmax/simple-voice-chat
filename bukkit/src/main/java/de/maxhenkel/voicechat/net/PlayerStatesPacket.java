package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.NamespacedKeyUtil;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatesPacket implements Packet<PlayerStatesPacket> {

    private Map<UUID, PlayerState> playerStates;

    public static final NamespacedKey PLAYER_STATES = NamespacedKeyUtil.voicechat("player_states");

    public PlayerStatesPacket() {

    }

    public PlayerStatesPacket(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    @Override
    public NamespacedKey getID() {
        return PLAYER_STATES;
    }

    @Override
    public PlayerStatesPacket fromBytes(FriendlyByteBuf buf) {
        playerStates = new HashMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            PlayerState playerState = PlayerState.fromBytes(buf);
            playerStates.put(playerState.getUuid(), playerState);
        }

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
            entry.getValue().toBytes(buf);
        }
    }

}

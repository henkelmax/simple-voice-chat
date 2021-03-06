package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatesPacket {

    private Map<UUID, PlayerState> playerStates;

    public PlayerStatesPacket(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    public static PlayerStatesPacket fromBytes(PacketByteBuf buf) {
        int count = buf.readInt();
        Map<UUID, PlayerState> playerStates = new HashMap<>();
        for (int i = 0; i < count; i++) {
            playerStates.put(buf.readUuid(), new PlayerState(buf.readBoolean(), buf.readBoolean()));
        }

        return new PlayerStatesPacket(playerStates);
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
            buf.writeUuid(entry.getKey());
            buf.writeBoolean(entry.getValue().isDisabled());
            buf.writeBoolean(entry.getValue().isDisconnected());
        }
    }

}

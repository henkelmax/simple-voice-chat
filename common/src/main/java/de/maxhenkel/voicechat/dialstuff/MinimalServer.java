package de.maxhenkel.voicechat.dialstuff;

import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.ServerPlayer;

import java.util.*;

public class MinimalServer {

    Map<UUID, ServerPlayer> players = new HashMap<>();

    public int getPort() {
        return 25565;
    }

    public String getIp() {
        return "127.0.0.1";
    }

    public ServerPlayer getPlayer(UUID playerUUID) {
        return players.get(playerUUID);
    }

    public List<ServerPlayer> getPlayers() {
        return new ArrayList<>(players.values());
    }
}

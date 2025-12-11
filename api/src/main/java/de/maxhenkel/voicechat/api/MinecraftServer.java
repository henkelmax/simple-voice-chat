package de.maxhenkel.voicechat.api;

import java.util.List;
import java.util.UUID;

public interface MinecraftServer {

    public Object getMinecraftServer();
    /**
     * @return the port of the server
     */
    int getPort();

    /**
     * @return the ip of the server
     */
    String getIp();

    /**
     * @param playerUUID the UUID of the player
     * @return the corresponding {@link ServerPlayer} object
     */
    ServerPlayer getPlayer(UUID playerUUID);

    /**
     * @return a list of all players in the server
     */
    List<ServerPlayer> getPlayers();

    boolean isDedicated();

    boolean usesAuthentication();

    int getOperatorUserPermissionLevel();
}

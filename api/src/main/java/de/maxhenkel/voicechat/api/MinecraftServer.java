package de.maxhenkel.voicechat.api;

import java.util.List;
import java.util.UUID;

public interface MinecraftServer {

    /**
     * @return the actual server object
     */
    Object getMinecraftServer();

    /**
     * @return the port of the server
     */
    int getPort();

    /**
     * @return the ip of the server, without port
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

    /**
     * @return whether the server is a dedicated server
     */
    boolean isDedicated();

    /**
     * @return whether the server has 'online-mode' set to true
     */
    boolean usesAuthentication();

    /**
     * @return the permission level at which a user is considered an operator
     */
    int getOperatorUserPermissionLevel();

    /**
     * @return the available {@link  ServerLevel}s on the server.
     */
    List<ServerLevel> getServerLevels();
}

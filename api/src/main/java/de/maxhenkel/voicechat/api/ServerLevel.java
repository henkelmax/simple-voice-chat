package de.maxhenkel.voicechat.api;

import java.util.List;

public interface ServerLevel {

    /**
     * @return the actual level object
     */
    Object getServerLevel();

    /**
     * @return the list of players in the level
     */
    List<ServerPlayer> players();

    /**
     * @return the id of the level
     */
    String getResourceLocation();
}

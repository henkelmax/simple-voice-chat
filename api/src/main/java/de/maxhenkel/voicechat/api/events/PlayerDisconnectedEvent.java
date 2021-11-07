package de.maxhenkel.voicechat.api.events;

import java.util.UUID;

public interface PlayerDisconnectedEvent extends ServerEvent {

    /**
     * @return the UUID of the player
     */
    UUID getPlayerUuid();

}

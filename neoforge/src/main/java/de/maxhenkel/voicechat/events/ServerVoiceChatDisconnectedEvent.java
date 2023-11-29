package de.maxhenkel.voicechat.events;

import net.neoforged.bus.api.Event;

import java.util.UUID;

public class ServerVoiceChatDisconnectedEvent extends Event {

    private final UUID playerID;

    public ServerVoiceChatDisconnectedEvent(UUID playerID) {
        this.playerID = playerID;
    }

    public UUID getPlayerID() {
        return playerID;
    }

}

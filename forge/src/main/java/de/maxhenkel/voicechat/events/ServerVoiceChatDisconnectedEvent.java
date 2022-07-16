package de.maxhenkel.voicechat.events;

import net.minecraftforge.eventbus.api.GenericEvent;

import java.util.UUID;

public class ServerVoiceChatDisconnectedEvent extends GenericEvent<ServerVoiceChatDisconnectedEvent> {

    private final UUID playerID;

    public ServerVoiceChatDisconnectedEvent(UUID playerID) {
        this.playerID = playerID;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

}

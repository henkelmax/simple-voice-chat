package de.maxhenkel.voicechat.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public class ServerVoiceChatDisconnectedEvent extends Event {

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

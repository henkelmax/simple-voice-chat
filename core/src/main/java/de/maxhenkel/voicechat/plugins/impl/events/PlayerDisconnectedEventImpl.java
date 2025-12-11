package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;

import java.util.UUID;

public class PlayerDisconnectedEventImpl extends ServerEventImpl implements PlayerDisconnectedEvent {

    protected UUID player;

    public PlayerDisconnectedEventImpl(UUID player) {
        this.player = player;
    }

    @Override
    public UUID getPlayerUuid() {
        return player;
    }
}

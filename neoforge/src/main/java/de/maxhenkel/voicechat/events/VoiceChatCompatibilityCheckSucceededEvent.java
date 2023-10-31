package de.maxhenkel.voicechat.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VoiceChatCompatibilityCheckSucceededEvent extends Event {

    private final ServerPlayer player;

    public VoiceChatCompatibilityCheckSucceededEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}

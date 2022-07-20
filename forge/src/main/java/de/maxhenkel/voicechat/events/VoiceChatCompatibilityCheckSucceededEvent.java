package de.maxhenkel.voicechat.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.GenericEvent;

public class VoiceChatCompatibilityCheckSucceededEvent extends GenericEvent<VoiceChatCompatibilityCheckSucceededEvent> {

    private final ServerPlayer player;

    public VoiceChatCompatibilityCheckSucceededEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}

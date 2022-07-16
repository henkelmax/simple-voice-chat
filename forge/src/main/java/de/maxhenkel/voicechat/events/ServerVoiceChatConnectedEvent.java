package de.maxhenkel.voicechat.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.GenericEvent;

public class ServerVoiceChatConnectedEvent extends GenericEvent<ServerVoiceChatConnectedEvent> {

    private final ServerPlayer player;

    public ServerVoiceChatConnectedEvent(ServerPlayer player) {
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

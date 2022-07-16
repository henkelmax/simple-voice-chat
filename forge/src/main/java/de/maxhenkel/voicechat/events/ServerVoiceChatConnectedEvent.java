package de.maxhenkel.voicechat.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.GenericEvent;

public class ServerVoiceChatConnectedEvent extends GenericEvent<ServerVoiceChatConnectedEvent> {

    private final ServerPlayerEntity player;

    public ServerVoiceChatConnectedEvent(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}

package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;
import java.util.function.Consumer;

public class ServerVoiceChatEvents {

    public static final Event<Consumer<ServerPlayerEntity>> VOICECHAT_CONNECTED = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayerEntity> listener : listeners) {
            listener.accept(player);
        }
    });

    public static final Event<Consumer<UUID>> VOICECHAT_DISCONNECTED = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (uuid) -> {
        for (Consumer<UUID> listener : listeners) {
            listener.accept(uuid);
        }
    });

    public static final Event<Consumer<ServerPlayerEntity>> VOICECHAT_COMPATIBILITY_CHECK_SUCCEEDED = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayerEntity> listener : listeners) {
            listener.accept(player);
        }
    });

}

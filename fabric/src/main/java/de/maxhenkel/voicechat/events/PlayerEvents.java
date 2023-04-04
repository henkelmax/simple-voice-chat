package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.Consumer;

public class PlayerEvents {

    public static final Event<Consumer<ServerPlayerEntity>> PLAYER_LOGGED_IN = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayerEntity> listener : listeners) {
            listener.accept(player);
        }
    });

    public static final Event<Consumer<ServerPlayerEntity>> PLAYER_LOGGED_OUT = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayerEntity> listener : listeners) {
            listener.accept(player);
        }
    });
}

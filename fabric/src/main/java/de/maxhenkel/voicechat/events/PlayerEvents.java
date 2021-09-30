package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class PlayerEvents {

    public static final Event<Consumer<ServerPlayer>> PLAYER_LOGGED_IN = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });

    public static final Event<Consumer<ServerPlayer>> PLAYER_LOGGED_OUT = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });
}

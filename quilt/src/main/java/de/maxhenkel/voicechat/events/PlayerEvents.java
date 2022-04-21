package de.maxhenkel.voicechat.events;

import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.function.Consumer;

public class PlayerEvents {

    public static final Event<Consumer<ServerPlayer>> PLAYER_LOGGED_IN = Event.create(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });

    public static final Event<Consumer<ServerPlayer>> PLAYER_LOGGED_OUT = Event.create(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });
}

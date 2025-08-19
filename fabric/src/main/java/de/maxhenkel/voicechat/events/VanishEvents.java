package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class VanishEvents {

    public static final Event<BiConsumer<ServerPlayer, ServerPlayer>> ON_VANISH = EventFactory.createArrayBacked(BiConsumer.class, (listeners) -> (visibilityChangedPlayer, observingPlayer) -> {
        for (BiConsumer<ServerPlayer, ServerPlayer> listener : listeners) {
            listener.accept(visibilityChangedPlayer, observingPlayer);
        }
    });

    public static final Event<BiConsumer<ServerPlayer, ServerPlayer>> ON_UNVANISH = EventFactory.createArrayBacked(BiConsumer.class, (listeners) -> (visibilityChangedPlayer, observingPlayer) -> {
        for (BiConsumer<ServerPlayer, ServerPlayer> listener : listeners) {
            listener.accept(visibilityChangedPlayer, observingPlayer);
        }
    });

}

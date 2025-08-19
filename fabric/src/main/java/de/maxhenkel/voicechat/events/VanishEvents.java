package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.BiConsumer;

public class VanishEvents {

    public static final Event<BiConsumer<ServerPlayerEntity, ServerPlayerEntity>> ON_VANISH = EventFactory.createArrayBacked(BiConsumer.class, (listeners) -> (visibilityChangedPlayer, observingPlayer) -> {
        for (BiConsumer<ServerPlayerEntity, ServerPlayerEntity> listener : listeners) {
            listener.accept(visibilityChangedPlayer, observingPlayer);
        }
    });

    public static final Event<BiConsumer<ServerPlayerEntity, ServerPlayerEntity>> ON_UNVANISH = EventFactory.createArrayBacked(BiConsumer.class, (listeners) -> (visibilityChangedPlayer, observingPlayer) -> {
        for (BiConsumer<ServerPlayerEntity, ServerPlayerEntity> listener : listeners) {
            listener.accept(visibilityChangedPlayer, observingPlayer);
        }
    });

}

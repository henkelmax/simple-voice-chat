package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.function.Consumer;

public class PublishServerEvents {

    public static final Event<Consumer<Integer>> SERVER_PUBLISHED = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (port) -> {
        for (Consumer<Integer> listener : listeners) {
            listener.accept(port);
        }
    });
}

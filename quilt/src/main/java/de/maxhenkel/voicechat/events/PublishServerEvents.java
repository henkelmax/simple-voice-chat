package de.maxhenkel.voicechat.events;

import org.quiltmc.qsl.base.api.event.Event;

import java.util.function.Consumer;

public class PublishServerEvents {

    public static final Event<Consumer<Integer>> SERVER_PUBLISHED = Event.create(Consumer.class, (listeners) -> (port) -> {
        for (Consumer<Integer> listener : listeners) {
            listener.accept(port);
        }
    });
}

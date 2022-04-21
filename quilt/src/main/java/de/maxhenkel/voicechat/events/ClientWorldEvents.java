package de.maxhenkel.voicechat.events;

import org.quiltmc.qsl.base.api.event.Event;

public class ClientWorldEvents {

    public static final Event<Runnable> DISCONNECT = Event.create(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

    public static final Event<Runnable> JOIN_SERVER = Event.create(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });
}

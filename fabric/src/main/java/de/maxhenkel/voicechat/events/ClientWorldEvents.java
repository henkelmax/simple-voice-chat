package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ClientWorldEvents {

    public static final Event<Runnable> DISCONNECT = EventFactory.createArrayBacked(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

    public static final Event<Runnable> JOIN_SERVER = EventFactory.createArrayBacked(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });
}

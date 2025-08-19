package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.function.Consumer;

public class VanishEvents {

    public static final Event<Consumer<CommonCompatibilityManager.PlayerVisibilityEvent>> ON_VANISH = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (evt) -> {
        for (Consumer<CommonCompatibilityManager.PlayerVisibilityEvent> listener : listeners) {
            listener.accept(evt);
        }
    });

    public static final Event<Consumer<CommonCompatibilityManager.PlayerVisibilityEvent>> ON_UNVANISH = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (evt) -> {
        for (Consumer<CommonCompatibilityManager.PlayerVisibilityEvent> listener : listeners) {
            listener.accept(evt);
        }
    });

}

package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class KeyboardEvents {

    public static final Event<KeyEvent> KEY = EventFactory.createArrayBacked(KeyEvent.class, (listeners) -> (window, key, scancode) -> {
        for (KeyEvent event : listeners) {
            event.onKeyEvent(window, key, scancode);
        }
    });

    public interface KeyEvent {
        void onKeyEvent(long window, int key, int scancode);
    }

}

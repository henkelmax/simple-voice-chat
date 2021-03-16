package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class KeyEvents {

    public static final Event<KeyboardEvent> KEYBOARD_KEY = EventFactory.createArrayBacked(KeyboardEvent.class, (listeners) -> (window, key, scancode) -> {
        for (KeyboardEvent event : listeners) {
            event.onKeyboardEvent(window, key, scancode);
        }
    });

    public static final Event<MouseEvent> MOUSE_KEY = EventFactory.createArrayBacked(MouseEvent.class, (listeners) -> (window, button, action, mods) -> {
        for (MouseEvent event : listeners) {
            event.onMouseEvent(window, button, action, mods);
        }
    });

    public interface KeyboardEvent {
        void onKeyboardEvent(long window, int key, int scancode);
    }

    public interface MouseEvent {
        void onMouseEvent(long window, int button, int action, int mods);
    }

}

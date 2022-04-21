package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import org.quiltmc.qsl.base.api.event.Event;

public class InputEvents {

    public static final Event<ClientCompatibilityManager.KeyboardEvent> KEYBOARD_KEY = Event.create(ClientCompatibilityManager.KeyboardEvent.class, (listeners) -> (window, key, scancode) -> {
        for (ClientCompatibilityManager.KeyboardEvent event : listeners) {
            event.onKeyboardEvent(window, key, scancode);
        }
    });

    public static final Event<ClientCompatibilityManager.MouseEvent> MOUSE_KEY = Event.create(ClientCompatibilityManager.MouseEvent.class, (listeners) -> (window, button, action, mods) -> {
        for (ClientCompatibilityManager.MouseEvent event : listeners) {
            event.onMouseEvent(window, button, action, mods);
        }
    });

    public static final Event<Runnable> HANDLE_KEYBINDS = Event.create(Runnable.class, (listeners) -> () -> {
        for (Runnable event : listeners) {
            event.run();
        }
    });

}

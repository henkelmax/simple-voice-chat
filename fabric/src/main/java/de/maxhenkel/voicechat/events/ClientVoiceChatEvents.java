package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.function.Consumer;

public class ClientVoiceChatEvents {

    public static final Event<Consumer<ClientVoicechatConnection>> VOICECHAT_CONNECTED = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (client) -> {
        for (Consumer<ClientVoicechatConnection> listener : listeners) {
            listener.accept(client);
        }
    });

    public static final Event<Runnable> VOICECHAT_DISCONNECTED = EventFactory.createArrayBacked(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

}

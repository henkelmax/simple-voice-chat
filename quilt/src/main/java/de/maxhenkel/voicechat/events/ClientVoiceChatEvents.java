package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.function.Consumer;

public class ClientVoiceChatEvents {

    public static final Event<Consumer<ClientVoicechatConnection>> VOICECHAT_CONNECTED = Event.create(Consumer.class, (listeners) -> (client) -> {
        for (Consumer<ClientVoicechatConnection> listener : listeners) {
            listener.accept(client);
        }
    });

    public static final Event<Runnable> VOICECHAT_DISCONNECTED = Event.create(Runnable.class, (listeners) -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

}

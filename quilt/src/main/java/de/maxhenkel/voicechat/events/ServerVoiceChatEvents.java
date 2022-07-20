package de.maxhenkel.voicechat.events;

import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.UUID;
import java.util.function.Consumer;

public class ServerVoiceChatEvents {

    public static final Event<Consumer<ServerPlayer>> VOICECHAT_CONNECTED = Event.create(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });

    public static final Event<Consumer<UUID>> VOICECHAT_DISCONNECTED = Event.create(Consumer.class, (listeners) -> (uuid) -> {
        for (Consumer<UUID> listener : listeners) {
            listener.accept(uuid);
        }
    });

    public static final Event<Consumer<ServerPlayer>> VOICECHAT_COMPATIBILITY_CHECK_SUCCEEDED = Event.create(Consumer.class, (listeners) -> (player) -> {
        for (Consumer<ServerPlayer> listener : listeners) {
            listener.accept(player);
        }
    });

}

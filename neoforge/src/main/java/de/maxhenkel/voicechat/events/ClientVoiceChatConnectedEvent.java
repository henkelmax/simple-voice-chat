package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.neoforged.bus.api.Event;

public class ClientVoiceChatConnectedEvent extends Event {

    private final ClientVoicechatConnection client;

    public ClientVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        this.client = client;
    }

    public ClientVoicechatConnection getClient() {
        return client;
    }

}

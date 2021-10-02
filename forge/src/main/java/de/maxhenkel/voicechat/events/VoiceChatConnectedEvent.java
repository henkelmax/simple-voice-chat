package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraftforge.eventbus.api.GenericEvent;

public class VoiceChatConnectedEvent extends GenericEvent<VoiceChatConnectedEvent> {

    private final ClientVoicechatConnection client;

    public VoiceChatConnectedEvent(ClientVoicechatConnection client) {
        this.client = client;
    }

    public ClientVoicechatConnection getClient() {
        return client;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}

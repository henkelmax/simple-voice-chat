package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.voice.client.Client;
import net.minecraftforge.eventbus.api.GenericEvent;

public class VoiceChatConnectedEvent extends GenericEvent<VoiceChatConnectedEvent> {

    private final Client client;

    public VoiceChatConnectedEvent(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}

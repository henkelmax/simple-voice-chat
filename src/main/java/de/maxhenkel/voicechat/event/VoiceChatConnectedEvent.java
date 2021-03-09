package de.maxhenkel.voicechat.event;

import de.maxhenkel.voicechat.voice.client.Client;
import net.minecraftforge.eventbus.api.Event;

public class VoiceChatConnectedEvent extends Event {

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

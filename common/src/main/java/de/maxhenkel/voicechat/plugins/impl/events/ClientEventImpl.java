package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientEvent;

public class ClientEventImpl extends EventImpl implements ClientEvent {

    private final VoicechatClientApi api;

    public ClientEventImpl(VoicechatClientApi api) {
        this.api = api;
    }

    @Override
    public VoicechatClientApi getVoicechat() {
        return api;
    }
}

package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.ServerEvent;

public class ServerEventImpl extends EventImpl implements ServerEvent {

    private final VoicechatServerApi api;

    public ServerEventImpl(VoicechatServerApi api) {
        this.api = api;
    }

    @Override
    public VoicechatServerApi getVoicechat() {
        return api;
    }

}

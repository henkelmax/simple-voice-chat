package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class VoicechatServerStartedEventImpl extends ServerEventImpl implements VoicechatServerStartedEvent {

    public VoicechatServerStartedEventImpl(VoicechatServerApi api) {
        super(api);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

public class VoicechatServerStoppedEventImpl extends ServerEventImpl implements VoicechatServerStoppedEvent {

    public VoicechatServerStoppedEventImpl(VoicechatServerApi api) {
        super(api);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

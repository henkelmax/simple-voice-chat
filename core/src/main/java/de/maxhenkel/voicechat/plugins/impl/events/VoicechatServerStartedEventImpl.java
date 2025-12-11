package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class VoicechatServerStartedEventImpl extends ServerEventImpl implements VoicechatServerStartedEvent {

    @Override
    public boolean isCancellable() {
        return false;
    }
}

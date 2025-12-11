package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

public class VoicechatServerStoppedEventImpl extends ServerEventImpl implements VoicechatServerStoppedEvent {

    @Override
    public boolean isCancellable() {
        return false;
    }
}

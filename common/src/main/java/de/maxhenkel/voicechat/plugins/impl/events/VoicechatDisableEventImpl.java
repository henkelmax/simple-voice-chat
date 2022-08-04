package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent;

public class VoicechatDisableEventImpl extends ClientEventImpl implements VoicechatDisableEvent {

    private final boolean disabled;

    public VoicechatDisableEventImpl(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }
}

package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent;

public class VoicechatDisableEventImpl extends ClientEventImpl implements VoicechatDisableEvent {

    private final boolean disabled;

    public VoicechatDisableEventImpl(VoicechatClientApi api, boolean disabled) {
        super(api);
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }
}

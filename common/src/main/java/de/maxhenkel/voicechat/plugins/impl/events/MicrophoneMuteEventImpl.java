package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;

public class MicrophoneMuteEventImpl extends ClientEventImpl implements MicrophoneMuteEvent {

    private final boolean muted;

    public MicrophoneMuteEventImpl(VoicechatClientApi api, boolean muted) {
        super(api);
        this.muted = muted;
    }

    @Override
    public boolean isDisabled() {
        return muted;
    }
}

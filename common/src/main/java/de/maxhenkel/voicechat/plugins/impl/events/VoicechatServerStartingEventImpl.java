package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartingEvent;

import javax.annotation.Nullable;

public class VoicechatServerStartingEventImpl extends ServerEventImpl implements VoicechatServerStartingEvent {

    @Nullable
    private VoicechatSocket socketImplementation;

    public VoicechatServerStartingEventImpl(VoicechatServerApi api) {
        super(api);
    }

    @Override
    public void setSocketImplementation(VoicechatSocket socket) {
        this.socketImplementation = socket;
    }

    @Nullable
    @Override
    public VoicechatSocket getSocketImplementation() {
        return socketImplementation;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

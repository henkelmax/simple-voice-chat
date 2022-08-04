package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;

import javax.annotation.Nullable;

public class ClientVoicechatInitializationEventImpl extends ClientEventImpl implements ClientVoicechatInitializationEvent {

    @Nullable
    private ClientVoicechatSocket socketImplementation;

    public ClientVoicechatInitializationEventImpl(VoicechatClientApi api) {
        super(api);
    }

    @Override
    public void setSocketImplementation(ClientVoicechatSocket socket) {
        this.socketImplementation = socket;
    }

    @Nullable
    @Override
    public ClientVoicechatSocket getSocketImplementation() {
        return socketImplementation;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

}

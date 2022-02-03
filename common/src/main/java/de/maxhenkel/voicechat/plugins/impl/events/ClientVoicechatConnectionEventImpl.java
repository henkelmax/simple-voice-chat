package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;

public class ClientVoicechatConnectionEventImpl extends ClientEventImpl implements ClientVoicechatConnectionEvent {

    private final boolean connected;

    public ClientVoicechatConnectionEventImpl(VoicechatClientApi api, boolean connected) {
        super(api);
        this.connected = connected;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}

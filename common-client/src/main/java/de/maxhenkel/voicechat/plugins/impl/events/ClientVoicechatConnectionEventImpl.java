package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;

public class ClientVoicechatConnectionEventImpl extends ClientEventImpl implements ClientVoicechatConnectionEvent {

    private final boolean connected;

    public ClientVoicechatConnectionEventImpl(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}

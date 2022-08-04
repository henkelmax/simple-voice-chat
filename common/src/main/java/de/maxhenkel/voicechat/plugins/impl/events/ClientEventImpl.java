package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientEvent;
import de.maxhenkel.voicechat.plugins.impl.VoicechatClientApiImpl;

public class ClientEventImpl extends EventImpl implements ClientEvent {

    @Override
    public VoicechatClientApi getVoicechat() {
        return VoicechatClientApiImpl.instance();
    }
}

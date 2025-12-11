package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.ServerEvent;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;

public class ServerEventImpl extends EventImpl implements ServerEvent {

    @Override
    public VoicechatServerApi getVoicechat() {
        return UncommonCompatibilityManager.INSTANCE.getServerApi();
    }

}

package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.JoinGroupEvent;

public class JoinGroupEventImpl extends GroupEventImpl implements JoinGroupEvent {

    public JoinGroupEventImpl(VoicechatServerApi api, Group group, VoicechatConnection connection) {
        super(api, group, connection);
    }
}

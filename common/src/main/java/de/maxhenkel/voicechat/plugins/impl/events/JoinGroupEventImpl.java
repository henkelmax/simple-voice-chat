package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.JoinGroupEvent;

public class JoinGroupEventImpl extends GroupEventImpl implements JoinGroupEvent {

    public JoinGroupEventImpl(Group group, VoicechatConnection connection) {
        super(group, connection);
    }
}

package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.LeaveGroupEvent;

import javax.annotation.Nullable;

public class LeaveGroupEventImpl extends GroupEventImpl implements LeaveGroupEvent {

    public LeaveGroupEventImpl(@Nullable Group group, VoicechatConnection connection) {
        super(group, connection);
    }
}

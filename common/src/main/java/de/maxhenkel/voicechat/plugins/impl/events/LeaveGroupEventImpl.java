package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.LeaveGroupEvent;

import javax.annotation.Nullable;

public class LeaveGroupEventImpl extends GroupEventImpl implements LeaveGroupEvent {

    public LeaveGroupEventImpl(VoicechatServerApi api, @Nullable Group group, VoicechatConnection connection) {
        super(api, group, connection);
    }
}

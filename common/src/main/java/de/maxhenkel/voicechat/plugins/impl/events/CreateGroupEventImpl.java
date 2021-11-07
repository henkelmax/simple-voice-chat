package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.CreateGroupEvent;

public class CreateGroupEventImpl extends GroupEventImpl implements CreateGroupEvent {

    public CreateGroupEventImpl(VoicechatServerApi api, Group group, VoicechatConnection connection) {
        super(api, group, connection);
    }
}

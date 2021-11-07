package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.GroupEvent;

import javax.annotation.Nullable;

public class GroupEventImpl extends ServerEventImpl implements GroupEvent {

    @Nullable
    protected Group group;
    protected VoicechatConnection connection;

    public GroupEventImpl(VoicechatServerApi api, @Nullable Group group, VoicechatConnection connection) {
        super(api);
        this.group = group;
        this.connection = connection;
    }

    @Nullable
    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public VoicechatConnection getConnection() {
        return connection;
    }
}

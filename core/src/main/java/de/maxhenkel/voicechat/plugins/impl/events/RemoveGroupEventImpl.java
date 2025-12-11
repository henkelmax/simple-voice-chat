package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.events.RemoveGroupEvent;

public class RemoveGroupEventImpl extends GroupEventImpl implements RemoveGroupEvent {

    public RemoveGroupEventImpl(Group group) {
        super(group, null);
    }

    @Override
    public boolean isCancellable() {
        return super.isCancellable() && group.isPersistent();
    }
}

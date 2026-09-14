package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.ForceShowIconsEvent;

public class ForceShowIconsEventImpl extends ClientEventImpl implements ForceShowIconsEvent {

    private boolean forceShow;

    @Override
    public void forceShow() {
        forceShow = true;
    }

    public boolean shouldForceShow() {
        return forceShow;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

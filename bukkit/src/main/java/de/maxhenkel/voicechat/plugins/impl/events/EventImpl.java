package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.Event;

public class EventImpl implements Event {

    private boolean cancelled;

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean cancel() {
        if (!isCancellable()) {
            return false;
        }
        cancelled = true;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}

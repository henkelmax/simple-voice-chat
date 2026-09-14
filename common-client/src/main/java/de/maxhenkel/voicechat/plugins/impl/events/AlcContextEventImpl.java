package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.AlcContextEvent;

public class AlcContextEventImpl extends ClientEventImpl implements AlcContextEvent {

    private long context;

    public AlcContextEventImpl(long context) {
        this.context = context;
    }

    @Override
    public void setContext(long context) {
        this.context = context;
    }

    public long getContext() {
        return context;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

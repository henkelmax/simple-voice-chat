package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.DestroyOpenALContextEvent;

public class DestroyOpenALContextEventImpl extends ClientEventImpl implements DestroyOpenALContextEvent {

    protected long context;
    protected long device;

    public DestroyOpenALContextEventImpl(long context, long device) {
        this.context = context;
        this.device = device;
    }

    @Override
    public long getContext() {
        return context;
    }

    @Override
    public long getDevice() {
        return device;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

}

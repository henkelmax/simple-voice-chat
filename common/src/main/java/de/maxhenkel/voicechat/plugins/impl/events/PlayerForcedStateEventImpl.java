package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent;

public class PlayerForcedStateEventImpl extends ServerEventImpl implements PlayerForcedStateEvent {

    private final VoicechatConnection connection;
    private boolean forceMuted;
    private boolean forceDisabled;

    public PlayerForcedStateEventImpl(VoicechatConnection connection, boolean forceMuted, boolean forceDisabled) {
        this.connection = connection;
        this.forceMuted = forceMuted;
        this.forceDisabled = forceDisabled;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public VoicechatConnection getConnection() {
        return connection;
    }

    @Override
    public boolean isForceMuted() {
        return forceMuted;
    }

    @Override
    public boolean isForceDisabled() {
        return forceDisabled;
    }

    @Override
    public void forceMute() {
        forceMuted = true;
    }

    @Override
    public void forceDisable() {
        forceDisabled = true;
    }
}

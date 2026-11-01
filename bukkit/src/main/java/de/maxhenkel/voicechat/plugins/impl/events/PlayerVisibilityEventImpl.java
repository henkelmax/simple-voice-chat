package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.events.PlayerVisibilityEvent;

public class PlayerVisibilityEventImpl extends ServerEventImpl implements PlayerVisibilityEvent {

    private final ServerPlayer observer;
    private final ServerPlayer player;
    private boolean hidden;

    public PlayerVisibilityEventImpl(ServerPlayer observer, ServerPlayer player, boolean hidden) {
        this.observer = observer;
        this.player = player;
        this.hidden = hidden;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public ServerPlayer getObserver() {
        return observer;
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void hide() {
        hidden = true;
    }
}

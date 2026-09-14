package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.HudIconRenderEvent;

public class HudIconRenderEventImpl extends ClientEventImpl implements HudIconRenderEvent {

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}

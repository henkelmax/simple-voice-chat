package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.NameTagIconRenderEvent;

import java.util.UUID;

public class NameTagIconRenderEventImpl extends ClientEventImpl implements NameTagIconRenderEvent {

    private UUID entityId;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }
}

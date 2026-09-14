package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.NameTagIconRenderEvent;
import de.maxhenkel.voicechat.api.internal.events.NameTagIconRenderEventExtension;

import java.util.UUID;

public class NameTagIconRenderEventImpl extends ClientEventImpl implements NameTagIconRenderEvent, NameTagIconRenderEventExtension {

    private UUID entityId;
    private boolean disconnected;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}

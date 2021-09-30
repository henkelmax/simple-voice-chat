package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class RenderNameplateEvents {

    public static final Event<ClientCompatibilityManager.RenderNameplateEvent> RENDER_NAMEPLATE = EventFactory.createArrayBacked(ClientCompatibilityManager.RenderNameplateEvent.class, (listeners) -> (entity, component, stack, vertexConsumers, light) -> {
        for (ClientCompatibilityManager.RenderNameplateEvent listener : listeners) {
            listener.render(entity, component, stack, vertexConsumers, light);
        }
    });

}

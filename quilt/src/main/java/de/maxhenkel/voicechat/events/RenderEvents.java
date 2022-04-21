package de.maxhenkel.voicechat.events;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.function.Consumer;

public class RenderEvents {

    public static final Event<ClientCompatibilityManager.RenderNameplateEvent> RENDER_NAMEPLATE = Event.create(ClientCompatibilityManager.RenderNameplateEvent.class, (listeners) -> (entity, component, stack, vertexConsumers, light) -> {
        for (ClientCompatibilityManager.RenderNameplateEvent listener : listeners) {
            listener.render(entity, component, stack, vertexConsumers, light);
        }
    });

    public static final Event<Consumer<PoseStack>> RENDER_HUD = Event.create(Consumer.class, (listeners) -> (poseStack) -> {
        for (Consumer<PoseStack> listener : listeners) {
            listener.accept(poseStack);
        }
    });

}

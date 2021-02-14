package de.maxhenkel.voicechat.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class RenderEvents {

    public static final Event<RenderNameplate> RENDER_NAMEPLATE = EventFactory.createArrayBacked(RenderNameplate.class, (listeners) -> (entity, stack, vertexConsumers, light) -> {
        for (RenderNameplate listener : listeners) {
            listener.render(entity, stack, vertexConsumers, light);
        }
    });

    public static interface RenderNameplate {
        void render(Entity entity, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light);
    }

}

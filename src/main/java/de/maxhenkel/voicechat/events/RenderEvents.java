package de.maxhenkel.voicechat.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

public class RenderEvents {

    public static final Event<RenderNameplate> RENDER_NAMEPLATE = EventFactory.createArrayBacked(RenderNameplate.class, (listeners) -> (entity, stack, vertexConsumers, light) -> {
        for (RenderNameplate listener : listeners) {
            listener.render(entity, stack, vertexConsumers, light);
        }
    });

    public static interface RenderNameplate {
        void render(Entity entity, PoseStack stack, MultiBufferSource bufferSource, int light);
    }

}

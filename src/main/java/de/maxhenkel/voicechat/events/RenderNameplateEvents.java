package de.maxhenkel.voicechat.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class RenderNameplateEvents {

    public static final Event<RenderNameplate> RENDER_NAMEPLATE = EventFactory.createArrayBacked(RenderNameplate.class, (listeners) -> (entity, component, stack, vertexConsumers, light) -> {
        for (RenderNameplate listener : listeners) {
            listener.render(entity, component, stack, vertexConsumers, light);
        }
    });

    public static interface RenderNameplate {
        void render(Entity entity, Component component, PoseStack stack, MultiBufferSource bufferSource, int light);
    }

}

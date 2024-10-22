package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = 10000)
public abstract class EntityRendererMixin {

    @Inject(method = "renderNameTag", at = @At("HEAD"))
    private void renderNameTag(EntityRenderState entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo info) {
        if (info.isCancelled()) {
            return;
        }
        if (entityRenderState.nameTag == null) {
            return;
        }
        RenderEvents.RENDER_NAMEPLATE.invoker().render(entityRenderState, component, poseStack, multiBufferSource, light);
    }

}
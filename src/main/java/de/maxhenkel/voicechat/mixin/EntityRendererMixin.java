package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "render")
    private void onRenderName(Entity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo info) {
        if (shouldShowName(entity)) {
            RenderEvents.RENDER_NAMEPLATE.invoker().render(entity, poseStack, multiBufferSource, light);
        }
    }

    @Shadow
    protected abstract boolean shouldShowName(Entity entity);

}

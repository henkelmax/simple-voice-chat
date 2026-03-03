package de.maxhenkel.voicechat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @WrapOperation(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V"))
    private void submitNameTag(AvatarRenderer instance, EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, int light, Operation<Void> original, @Local(argsOnly = true, name = "state") AvatarRenderState state) {
        original.call(instance, entityRenderState, poseStack, submitNodeCollector, cameraRenderState, light);
        RenderEvents.RENDER_NAMEPLATE.invoker().render(state, cameraRenderState, poseStack, submitNodeCollector);
    }

}
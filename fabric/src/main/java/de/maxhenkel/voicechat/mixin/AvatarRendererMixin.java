package de.maxhenkel.voicechat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 1))
    private void submitNameTag(SubmitNodeCollector instance, PoseStack stack, Vec3 pos, int offset, Component component, boolean discrete, int light, double distance, CameraRenderState cameraRenderState, Operation<Void> original, @Local(argsOnly = true) AvatarRenderState state, @Local(argsOnly = true) SubmitNodeCollector submitNodeCollector) {
        original.call(instance, stack, pos, offset, component, discrete, light, distance, cameraRenderState);
        RenderEvents.RENDER_NAMEPLATE.invoker().render(state, cameraRenderState, stack, submitNodeCollector);
    }

}
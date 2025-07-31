package de.maxhenkel.voicechat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagFeatureRendererMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V", ordinal = 1))
    public void render(Font instance, Component component, float x, float y, int color, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int backgroundColor, int light, Operation<Void> original, @Local SubmitNodeStorage.NameTagSubmit nameTagSubmit) {
        original.call(instance, component, x, y, color, bl, matrix4f, multiBufferSource, displayMode, backgroundColor, light);
        Component nameTag = nameTagSubmit.text();
        HoverEvent hoverEvent = nameTag.getStyle().getHoverEvent();
        if (hoverEvent == null) {
            return;
        }
        if (!(hoverEvent instanceof HoverEvent.ShowEntity showEntity)) {
            return;
        }
        PoseStack.Pose pose = new PoseStack.Pose();
        pose.mulPose(nameTagSubmit.pose());
        RenderEvents.RENDER_NAMEPLATE.invoker().render(showEntity.entity().uuid, color != -1, nameTag, pose, multiBufferSource, nameTagSubmit.lightCoords());
    }

}

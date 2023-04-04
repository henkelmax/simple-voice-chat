package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.gui.IngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IngameGui.class, priority = 999)
public class GuiMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/IngameGui;renderEffects(Lcom/mojang/blaze3d/matrix/MatrixStack;)V"))
    private void onHUDRender(MatrixStack poseStack, float f, CallbackInfo ci) {
        RenderEvents.RENDER_HUD.invoker().accept(poseStack);
    }

}

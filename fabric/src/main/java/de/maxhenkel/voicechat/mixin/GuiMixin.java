package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 999)
public class GuiMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void onHUDRender(PoseStack poseStack, float f, CallbackInfo ci) {
        RenderEvents.RENDER_HUD.invoker().accept(poseStack);
    }

}

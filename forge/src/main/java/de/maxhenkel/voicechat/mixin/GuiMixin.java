package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 999)
public class GuiMixin {

    @Inject(method = "renderEffects", at = @At(value = "HEAD"))
    private void renderEffects(GuiGraphics poseStack, float f, CallbackInfo ci) {
        ((ForgeClientCompatibilityManager) ForgeClientCompatibilityManager.INSTANCE).onRenderOverlay(poseStack, f);
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 999)
public class GuiMixin {

    @Inject(method = "renderEffects", at = @At(value = "HEAD"))
    private void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ((ForgeClientCompatibilityManager) ForgeClientCompatibilityManager.INSTANCE).onRenderOverlay(guiGraphics, deltaTracker.getRealtimeDeltaTicks());
    }

}

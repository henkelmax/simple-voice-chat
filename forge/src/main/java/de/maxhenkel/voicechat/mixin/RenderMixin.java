package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class RenderMixin {

    @Inject(at = @At("HEAD"), method = "renderLivingLabel")
    public void renderLivingLabel(Entity entity, String str, double x, double y, double z, int maxDistance, CallbackInfo info) {
        if (info.isCancelled()) {
            return;
        }

        //TODO Check show name conditions

        if (!entity.getDisplayName().getFormattedText().equals(str)) {
            return;
        }

        ((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE).onRenderName(entity, str, x, y, z, maxDistance);
    }

}

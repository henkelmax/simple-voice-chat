package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
        RenderEvents.RENDER_TICK.invoker().run();
    }

}

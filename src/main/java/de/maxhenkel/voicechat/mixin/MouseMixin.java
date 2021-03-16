package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.KeyEvents;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(at = @At("HEAD"), method = "onMouseButton")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        KeyEvents.MOUSE_KEY.invoker().onMouseEvent(window, button, action, mods);
    }

}

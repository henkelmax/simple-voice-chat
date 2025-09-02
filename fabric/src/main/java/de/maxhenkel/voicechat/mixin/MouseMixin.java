package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.InputEvents;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long windowHandle, MouseButtonInfo mouseButtonInfo, int action, CallbackInfo ci) {
        InputEvents.MOUSE_KEY.invoker().onMouseEvent(mouseButtonInfo, action);
    }

}

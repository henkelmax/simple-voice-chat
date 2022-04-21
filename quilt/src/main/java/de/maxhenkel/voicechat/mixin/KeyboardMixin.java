package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.InputEvents;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "keyPress")
    private void onKey(long window, int key, int scancode, int i, int j, CallbackInfo info) {
        InputEvents.KEYBOARD_KEY.invoker().onKeyboardEvent(window, key, scancode);
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.KeyEvents;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "onKey")
    private void onKey(long window, int key, int scancode, int i, int j, CallbackInfo info) {
        KeyEvents.KEYBOARD_KEY.invoker().onKeyboardEvent(window, key, scancode);
    }

}

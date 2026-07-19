package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.InputEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift =  At.Shift.AFTER))
    private void onKey(long windowHandle, int key, KeyEvent keyEvent, CallbackInfo ci) {
        InputEvents.KEYBOARD_KEY.invoker().onKeyboardEvent(keyEvent);
    }

}

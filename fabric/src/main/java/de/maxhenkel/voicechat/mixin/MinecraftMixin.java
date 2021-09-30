package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.ClientWorldEvents;
import de.maxhenkel.voicechat.events.InputEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public ClientLevel level;

    @Inject(at = @At("HEAD"), method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void disconnect(Screen screen, CallbackInfo info) {
        if (level != null) {
            ClientWorldEvents.DISCONNECT.invoker().run();
        }
    }

    @Inject(at = @At("HEAD"), method = "handleKeybinds")
    private void handleKeybinds(CallbackInfo info) {
        InputEvents.HANDLE_KEYBINDS.invoker().run();
    }

}

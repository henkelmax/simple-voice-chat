package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTickMouse", at = @At("RETURN"))
    public void runTickMouse(CallbackInfo info) {
        ((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE).onTickMouse();
    }

    @Inject(method = "runTickKeyboard", at = @At("RETURN"))
    public void runTickKeyboard(CallbackInfo info) {
        ((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE).onTickKey();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    public void loadWorld(WorldClient world, String msg, CallbackInfo info) {
        if (world == null) {
            ((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE).onDisconnect();
        }
    }

}

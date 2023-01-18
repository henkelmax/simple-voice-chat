package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "shareToLAN", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setPermissionLevel(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void shareToLAN(GameType type, boolean allowCheats, CallbackInfoReturnable<String> cir, int port) {
        ((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE).onOpenPort(port);
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.PublishServerEvents;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @Inject(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z", at = @At(value = "RETURN"))
    public void publishServer(MinecraftServer.MultiplayerScope scope, int port, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }
        PublishServerEvents.SERVER_PUBLISHED.invoker().accept(port);
    }

}

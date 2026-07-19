package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.PublishServerEvents;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @Inject(method = "publishServer", at = @At(value = "RETURN"))
    public void publishServer(MinecraftServer.MultiplayerScope scope, @Nullable GameType gameMode, boolean allowCommands, int port, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }
        PublishServerEvents.SERVER_PUBLISHED.invoker().accept(port);
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.PlayerEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {


    @Inject(at = @At("RETURN"), method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_IN.invoker().accept(player);
    }

    @Inject(at = @At("HEAD"), method = "remove")
    private void onPlayerConnect(ServerPlayerEntity player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_OUT.invoker().accept(player);
    }

}

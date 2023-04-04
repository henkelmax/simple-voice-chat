package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.PlayerEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {


    @Inject(at = @At("RETURN"), method = "placeNewPlayer")
    private void onPlayerConnect(NetworkManager connection, ServerPlayerEntity player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_IN.invoker().accept(player);
    }

    @Inject(at = @At("HEAD"), method = "remove")
    private void onPlayerConnect(ServerPlayerEntity player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_OUT.invoker().accept(player);
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.PlayerEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {


    @Inject(at = @At("RETURN"), method = "placeNewPlayer")
    private void onPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_IN.invoker().accept(player);
    }

    @Inject(at = @At("HEAD"), method = "remove")
    private void onPlayerConnect(ServerPlayer player, CallbackInfo info) {
        PlayerEvents.PLAYER_LOGGED_OUT.invoker().accept(player);
    }

}

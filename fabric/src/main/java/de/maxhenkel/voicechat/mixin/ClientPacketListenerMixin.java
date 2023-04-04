package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.ClientWorldEvents;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SJoinGamePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class ClientPacketListenerMixin {

    @Inject(at = @At("RETURN"), method = "handleLogin")
    private void createPlayer(SJoinGamePacket clientboundLoginPacket, CallbackInfo info) {
        ClientWorldEvents.JOIN_SERVER.invoker().run();
    }

}

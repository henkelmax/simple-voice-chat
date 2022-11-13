package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.net.ForgeNetworkEvents;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleCustomPayload(SPacketCustomPayload packet, CallbackInfo ci) {
        if (ForgeNetworkEvents.onCustomPayloadClient(packet)) {
            ci.cancel();
        }
    }

}

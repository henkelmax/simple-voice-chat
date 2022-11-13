package de.maxhenkel.voicechat.mixin;

import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SPacketCustomPayload.class)
public class SPacketCustomPayloadMixin {

    @ModifyConstant(method = "readPacketData", constant = @Constant(intValue = 20))
    private int injected(int value) {
        return 64;
    }

}

package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import net.minecraft.client.gui.GuiSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GuiSlot.class)
public class GuiSlotMixin {

    @ModifyConstant(method = "drawScreen", constant = @Constant(intValue = 255, ordinal = 4))
    private int alpha1(int value) {
        return isVoicechatList() ? 0 : 255;
    }

    @ModifyConstant(method = "drawScreen", constant = @Constant(intValue = 255, ordinal = 5))
    private int alpha2(int value) {
        return isVoicechatList() ? 0 : 255;
    }

    @ModifyConstant(method = "drawScreen", constant = @Constant(intValue = 255, ordinal = 6))
    private int alpha3(int value) {
        return isVoicechatList() ? 0 : 255;
    }

    @ModifyConstant(method = "drawScreen", constant = @Constant(intValue = 255, ordinal = 7))
    private int alpha4(int value) {
        return isVoicechatList() ? 0 : 255;
    }

    private boolean isVoicechatList() {
        return ((Object) this) instanceof ListScreenListBase;
    }

}

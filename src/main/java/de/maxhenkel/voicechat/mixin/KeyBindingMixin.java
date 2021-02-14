package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.IKeyBinding;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements IKeyBinding {

    @Shadow
    private InputUtil.Key boundKey;


    @Override
    public InputUtil.Key getBoundKey() {
        return boundKey;
    }
}

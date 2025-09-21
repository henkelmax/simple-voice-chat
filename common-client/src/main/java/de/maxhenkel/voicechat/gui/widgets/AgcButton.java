package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.SpeexManager;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final Component AUTO = Component.translatable("message.voicechat.gain.auto");
    private static final Component MANUAL = Component.translatable("message.voicechat.gain.manual").withStyle(ChatFormatting.RED);
    private static final Tooltip MANUAL_WARNING = Tooltip.create(Component.translatable("message.voicechat.gain.manual.warning").withStyle(ChatFormatting.RED));

    private final Consumer<Boolean> onChange;

    public AgcButton(int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            MutableComponent translatable = Component.translatable("message.voicechat.gain", enabled ? AUTO : MANUAL);
            if (!enabled) {
                translatable.withStyle(ChatFormatting.RED);
            }
            return translatable;
        });
        this.onChange = onChange;
        if (!SpeexManager.canUseAgc()) {
            active = false;
            onChange.accept(false);
        } else {
            onChange.accept(entry.get());
        }
        updateTooltip();
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        super.onPress(inputWithModifiers);
        onChange.accept(entry.get());
        updateTooltip();
    }

    private void updateTooltip() {
        if (!entry.get()) {
            setTooltip(MANUAL_WARNING);
        } else {
            setTooltip(null);
        }
    }
}

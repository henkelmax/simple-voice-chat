package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.SpeexManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final ITextComponent AUTO = new TranslationTextComponent("message.voicechat.gain.auto");
    private static final ITextComponent MANUAL = new TranslationTextComponent("message.voicechat.gain.manual").withStyle(TextFormatting.RED);
    private static final ITextComponent MANUAL_WARNING = new TranslationTextComponent("message.voicechat.gain.manual.warning").withStyle(TextFormatting.RED);

    private final Consumer<Boolean> onChange;
    private final Screen parent;

    public AgcButton(Screen parent, int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            TranslationTextComponent translatable = new TranslationTextComponent("message.voicechat.gain", enabled ? AUTO : MANUAL);
            if (!enabled) {
                translatable.withStyle(TextFormatting.RED);
            }
            return translatable;
        });
        this.parent = parent;
        this.onChange = onChange;
        if (!SpeexManager.canUseAgc()) {
            active = false;
            onChange.accept(false);
        } else {
            onChange.accept(entry.get());
        }
    }

    @Override
    public void onPress() {
        super.onPress();
        onChange.accept(entry.get());
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int i, int j, float f) {
        super.renderButton(matrixStack, i, j, f);
        if (isHovered()) {
            renderToolTip(matrixStack, i, j);
        }
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int i, int j) {
        super.renderToolTip(matrixStack, i, j);
        if (!entry.get()) {
            parent.renderTooltip(matrixStack, Minecraft.getInstance().font.split(MANUAL_WARNING, 200), i, j);
        }
    }

}

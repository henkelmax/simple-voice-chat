package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.SpeexManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final ITextComponent AUTO = new TextComponentTranslation("message.voicechat.gain.auto");
    private static final ITextComponent MANUAL = new TextComponentTranslation("message.voicechat.gain.manual").setStyle(new Style().setColor(TextFormatting.RED));
    private static final ITextComponent MANUAL_WARNING = new TextComponentTranslation("message.voicechat.gain.manual.warning").setStyle(new Style().setColor(TextFormatting.RED));

    private final Consumer<Boolean> onChange;
    private final GuiScreen parent;

    public AgcButton(int id, GuiScreen parent, int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            TextComponentTranslation translatable = new TextComponentTranslation("message.voicechat.gain", enabled ? AUTO : MANUAL);
            if (!enabled) {
                translatable.setStyle(new Style().setColor(TextFormatting.RED));
            }
            return translatable;
        });
        this.parent = parent;
        this.onChange = onChange;
        if (!SpeexManager.canUseAgc()) {
            enabled = false;
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
    public void renderTooltips(int mouseX, int mouseY, float delta) {
        super.renderTooltips(mouseX, mouseY, delta);
        if (!isMouseOver()) {
            return;
        }
        if (!entry.get()) {
            parent.drawHoveringText(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(MANUAL_WARNING.getFormattedText(), 200), mouseX, mouseY);
        }
    }

}

package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class MicAmplificationSlider extends DebouncedSlider {

    public static final int GAIN_WARNING_THRESHOLD = 10;
    private static final ITextComponent GAIN_WARNING = new TextComponentTranslation("message.voicechat.microphone_gain.warning", GAIN_WARNING_THRESHOLD).setStyle(new Style().setColor(TextFormatting.RED));

    private final GuiScreen parent;

    public MicAmplificationSlider(int id, GuiScreen parent, int xIn, int yIn, int widthIn, int heightIn) {
        super(id, xIn, yIn, widthIn, heightIn, gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        this.parent = parent;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        TextComponentTranslation message = new TextComponentTranslation("message.voicechat.microphone_gain", gain);
        if (gain > GAIN_WARNING_THRESHOLD && enabled) {
            message.setStyle(new Style().setColor(TextFormatting.RED));
        }
        displayString = message.getFormattedText();
    }

    @Override
    public void renderTooltips(int mouseX, int mouseY, float delta) {
        super.renderTooltips(mouseX, mouseY, delta);
        if (!isMouseOver()) {
            return;
        }
        long gain = Math.round(valueToGain(value));
        if (gain > GAIN_WARNING_THRESHOLD && enabled) {
            parent.drawHoveringText(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(GAIN_WARNING.getFormattedText(), 200), mouseX, mouseY);
        }
    }

    public void setActive(boolean active) {
        this.enabled = active;
        updateMessage();
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.microphoneGain.set(valueToGain(value)).save();
    }

    private static double gainToValue(double gain) {
        return (gain - VolumeManager.MIN_GAIN) / (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN);
    }

    private static double valueToGain(double value) {
        return value * (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN) + VolumeManager.MIN_GAIN;
    }

}

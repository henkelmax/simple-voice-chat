package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final ITextComponent NO_ACTIVATION = new TextComponentTranslation("message.voicechat.voice_activation.disabled").setStyle(new Style().setColor(TextFormatting.RED));

    private final SlidingAverage micValue;

    public VoiceActivationSlider(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingAverage();
    }

    @Override
    public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        super.mouseDragged(mc, mouseX, mouseY);
        mc.getTextureManager().bindTexture(SLIDER);
        GlStateManager.color(1F, 1F, 1F, 1F);
        int width = (int) ((getButtonWidth() - 2) * micValue.average());
        drawTexturedModalRect(x + 1, y + 1, 0, 0, width, 18);
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(percToDb(value));
        TextComponentTranslation component = new TextComponentTranslation("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.setStyle(new Style().setColor(TextFormatting.RED));
        }

        displayString = component.getFormattedText();
    }

    @Nullable
    public ITextComponent getHoverText() {
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(percToDb(value)).save();
    }

    @Override
    public void onMicValue(double dB) {
        micValue.add(dbToPerc(dB));
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public void onStop() {
        micValue.reset();
    }

    /**
     * Converts a dB value to a percentage value ({@link AudioUtils#LOWEST_DB} - 0) - (0 - 1)
     *
     * @param db the decibel value
     * @return the percentage
     */
    public static double dbToPerc(double db) {
        db = Math.min(Math.max(db, AudioUtils.LOWEST_DB), 0D);
        return (db + Math.abs(AudioUtils.LOWEST_DB)) / Math.abs(AudioUtils.LOWEST_DB);
    }

    /**
     * Converts a percentage to a dB value (0 - 1) - ({@link AudioUtils#LOWEST_DB} - 0)
     *
     * @param perc the percentage
     * @return the decibel value
     */
    public static double percToDb(double perc) {
        perc = Math.min(Math.max(perc, 0D), 1D);
        return (1D - perc) * AudioUtils.LOWEST_DB;
    }

    private static class SlidingAverage {
        private final double[] values = new double[5];
        private int n, p;
        private double s;

        public void add(double x) {
            if (n < values.length) {
                n++;
            } else {
                s -= values[p];
            }
            s += x;
            values[p] = x;
            p = (p + 1) % values.length;
        }

        public double average() {
            return n == 0 ? 0D : s / n;
        }

        public void reset() {
            n = 0;
            p = 0;
            s = 0D;
        }
    }

}

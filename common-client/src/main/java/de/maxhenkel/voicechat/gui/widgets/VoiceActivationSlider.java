package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final ITextComponent NO_ACTIVATION = new TranslationTextComponent("message.voicechat.voice_activation.disabled").withStyle(TextFormatting.RED);

    private final SlidingAverage micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""), dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingAverage();
    }

    @Override
    protected void renderBg(MatrixStack poseStack, Minecraft minecraft, int i, int j) {
        minecraft.getTextureManager().bind(SLIDER);
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        int width = (int) ((getWidth() - 2) * micValue.average());
        blit(poseStack, x + 1, y + 1, 0, 0, width, 18);
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(percToDb(value));
        TranslationTextComponent component = new TranslationTextComponent("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.withStyle(TextFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public ITextComponent getHoverText() {
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(percToDb(value)).save();
    }

    @Override
    public void onMicValue(double dB) {
        micValue.add(dbToPerc(dB));
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

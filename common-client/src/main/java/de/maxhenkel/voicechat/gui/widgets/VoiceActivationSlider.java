package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final ITextComponent NO_ACTIVATION = new TextComponentTranslation("message.voicechat.voice_activation.disabled").setStyle(new Style().setColor(TextFormatting.RED));

    private final SlidingAverage micValue;

    public VoiceActivationSlider(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, AudioUtils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingAverage();
    }

    public boolean shouldShowSlider() {
        if (!MicrophoneActivationType.VOICE.equals(VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get())) {
            return false;
        }
        if (!Denoiser.canUseDenoiser()) {
            return true;
        }
        return !VoicechatClient.CLIENT_CONFIG.vad.get();
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
    protected void renderSlider(Minecraft mc) {
        boolean shouldShow = shouldShowSlider();
        if (shouldShow != enabled) {
            enabled = shouldShow;
            updateMessage();
        }
        if (!enabled) {
            return;
        }
        super.renderSlider(mc);
    }

    @Override
    protected void updateMessage() {
        if (!enabled) {
            displayString = "";
            return;
        }
        long db = Math.round(AudioUtils.percToDb(value));
        TextComponentTranslation component = new TextComponentTranslation("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.setStyle(new Style().setColor(TextFormatting.RED));
        }

        displayString = component.getFormattedText();
    }

    @Nullable
    public ITextComponent getHoverText() {
        if (!enabled) {
            return null;
        }
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(AudioUtils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double dB) {
        micValue.add(AudioUtils.dbToPerc(dB));
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public void onStop() {
        micValue.reset();
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

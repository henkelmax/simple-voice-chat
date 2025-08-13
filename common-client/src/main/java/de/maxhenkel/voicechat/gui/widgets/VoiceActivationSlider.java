package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
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
        super(id, x, y, width, height, Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
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
        long db = Math.round(Utils.percToDb(value));
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
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double percentage) {
        micValue.add(percentage);
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

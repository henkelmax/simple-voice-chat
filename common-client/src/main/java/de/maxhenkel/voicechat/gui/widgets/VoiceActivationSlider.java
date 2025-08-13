package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
    private static final ResourceLocation VOICE_ACTIVATION_SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final Component NO_ACTIVATION = Component.translatable("message.voicechat.voice_activation.disabled").withStyle(ChatFormatting.RED);

    private final SlidingAverage micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingAverage();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blitNineSliced(SLIDER_LOCATION, getX(), getY(), getWidth(), getHeight(), 4, 200, 20, 0, getYTexturePos());

        int micWidth = (int) ((width - 2) * micValue.average());
        guiGraphics.blit(VOICE_ACTIVATION_SLIDER, getX() + 1, getY() + 1, 0, 0, micWidth, 18);

        guiGraphics.blitNineSliced(SLIDER_LOCATION, getX() + (int) (value * (double) (width - 8)), getY(), 8, 20, 4, 200, 20, 0, getYtexturePosHandle());
        int color = active ? 16777215 : 10526880;
        renderScrollingString(guiGraphics, minecraft.font, 2, color);
    }

    private int getYTexturePos() {
        return (isFocused() && !(isHovered || isFocused()) ? 1 : 0) * 20;
    }

    private int getYtexturePosHandle() {
        return (!isHovered && !isFocused() ? 2 : 3) * 20;
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(percToDb(value));
        MutableComponent component = Component.translatable("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.withStyle(ChatFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public Component getHoverText() {
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

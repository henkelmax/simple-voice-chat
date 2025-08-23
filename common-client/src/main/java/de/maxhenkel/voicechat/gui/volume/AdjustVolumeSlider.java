package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final Component MUTED = Component.translatable("message.voicechat.muted");

    protected static final double YELLOW_DB = -20D;
    protected static final double RED_DB = -6D;

    protected static final float MAXIMUM = 4F;

    protected final AdjustVolumeEntry volumeConfigEntry;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, AdjustVolumeEntry volumeConfigEntry) {
        super(xIn, yIn, widthIn, heightIn, Component.empty(), volumeConfigEntry.get() / MAXIMUM);
        this.volumeConfigEntry = volumeConfigEntry;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (value <= 0D) {
            setMessage(MUTED);
            return;
        }
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(Component.translatable("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderWidget(guiGraphics, i, j, f);
        double audioLevel = volumeConfigEntry.getAudioLevel();
        if (audioLevel <= AudioUtils.LOWEST_DB) {
            return;
        }
        double adjustedLevel = AudioUtils.linearToDb(getMultiplier());
        int barWidth = (int) ((double) getWidth() * AudioUtils.dbToPerc(audioLevel + adjustedLevel));
        double yellowPerc = AudioUtils.dbToPerc(YELLOW_DB);
        double redPerc = AudioUtils.dbToPerc(RED_DB);
        int greenWidth = (int) ((double) getWidth() * yellowPerc);
        int yellowWidth = (int) ((double) getWidth() * redPerc) - greenWidth;
        int width = getWidth();
        guiGraphics.fill(getX(), getY(), getX() + Math.min(greenWidth, barWidth), getY() + 1, 0xFF00FF00);
        if (barWidth > greenWidth) {
            guiGraphics.fill(getX() + greenWidth, getY(), getX() + Math.min(greenWidth + yellowWidth, barWidth), getY() + 1, 0xFFFFFF00);
            if (barWidth > greenWidth + yellowWidth) {
                guiGraphics.fill(getX() + greenWidth + yellowWidth, getY(), getX() + Math.min(width, barWidth), getY() + 1, 0xFFFF0000);
            }
        }
    }

    @Override
    public void applyDebounced() {
        volumeConfigEntry.save(getMultiplier());
    }

    private double getMultiplier() {
        return value * MAXIMUM;
    }

    public interface AdjustVolumeEntry {
        void save(double value);

        double get();

        double getAudioLevel();
    }

}

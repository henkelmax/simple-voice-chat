package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final ITextComponent MUTED = new TranslationTextComponent("message.voicechat.muted");

    protected static final double YELLOW_DB = -20D;
    protected static final double RED_DB = -6D;

    protected static final float MAXIMUM = 4F;

    protected final AdjustVolumeEntry volumeConfigEntry;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, AdjustVolumeEntry volumeConfigEntry) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), volumeConfigEntry.get() / MAXIMUM);
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
        setMessage(new TranslationTextComponent("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void render(MatrixStack matrixStack, int i, int j, float f) {
        super.render(matrixStack, i, j, f);
        double audioLevel = volumeConfigEntry.getAudioLevel();
        double adjustedLevel = AudioUtils.linearToDb(getMultiplier());
        int barWidth = (int) ((double) getWidth() * AudioUtils.dbToPerc(audioLevel + adjustedLevel));
        double yellowPerc = AudioUtils.dbToPerc(YELLOW_DB);
        double redPerc = AudioUtils.dbToPerc(RED_DB);
        int greenWidth = (int) ((double) getWidth() * yellowPerc);
        int yellowWidth = (int) ((double) getWidth() * redPerc) - greenWidth;
        int width = getWidth();

        Screen.fill(matrixStack, x, y, x + Math.min(greenWidth, barWidth), y + 1, 0xFF00FF00);
        if (barWidth > greenWidth) {
            Screen.fill(matrixStack, x + greenWidth, y, x + Math.min(greenWidth + yellowWidth, barWidth), y + 1, 0xFFFFFF00);
            if (barWidth > greenWidth + yellowWidth) {
                Screen.fill(matrixStack, x + greenWidth + yellowWidth, y, x + Math.min(width, barWidth), y + 1, 0xFFFF0000);
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

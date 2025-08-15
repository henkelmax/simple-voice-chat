package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final ITextComponent MUTED = new TextComponentTranslation("message.voicechat.muted");

    protected static final double YELLOW_DB = -20D;
    protected static final double RED_DB = -6D;

    protected static final float MAXIMUM = 4F;

    protected final AdjustVolumeEntry volumeConfigEntry;

    public AdjustVolumeSlider(int id, int xIn, int yIn, int widthIn, int heightIn, AdjustVolumeEntry volumeConfigEntry) {
        super(id, xIn, yIn, widthIn, heightIn, volumeConfigEntry.get() / MAXIMUM);
        this.volumeConfigEntry = volumeConfigEntry;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (value <= 0D) {
            displayString = MUTED.getUnformattedComponentText();
            return;
        }
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        displayString = new TextComponentTranslation("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%").getUnformattedComponentText();
    }


    @Override
    public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        super.mouseDragged(mc, mouseX, mouseY);
        double audioLevel = volumeConfigEntry.getAudioLevel();
        double adjustedLevel = AudioUtils.linearToDb(getMultiplier());
        int barWidth = (int) ((double) width * AudioUtils.dbToPerc(audioLevel + adjustedLevel));
        double yellowPerc = AudioUtils.dbToPerc(YELLOW_DB);
        double redPerc = AudioUtils.dbToPerc(RED_DB);
        int greenWidth = (int) ((double) width * yellowPerc);
        int yellowWidth = (int) ((double) width * redPerc) - greenWidth;

        GuiScreen.drawRect(x, y, x + Math.min(greenWidth, barWidth), y + 1, 0xFF00FF00);
        if (barWidth > greenWidth) {
            GuiScreen.drawRect(x + greenWidth, y, x + Math.min(greenWidth + yellowWidth, barWidth), y + 1, 0xFFFFFF00);
            if (barWidth > greenWidth + yellowWidth) {
                GuiScreen.drawRect(x + greenWidth + yellowWidth, y, x + Math.min(width, barWidth), y + 1, 0xFFFF0000);
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

package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final ITextComponent MUTED = new TranslationTextComponent("message.voicechat.muted");

    protected static final float MAXIMUM = 4F;

    protected final VolumeConfigEntry volumeConfigEntry;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, VolumeConfigEntry volumeConfigEntry) {
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
    public void applyDebounced() {
        volumeConfigEntry.save(value * MAXIMUM);
    }

    public interface VolumeConfigEntry {
        void save(double value);

        double get();
    }

}

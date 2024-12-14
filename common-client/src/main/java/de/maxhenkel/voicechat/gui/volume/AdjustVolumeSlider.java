package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final Component MUTED = new TranslatableComponent("message.voicechat.muted");

    protected static final float MAXIMUM = 4F;

    protected final VolumeConfigEntry volumeConfigEntry;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, VolumeConfigEntry volumeConfigEntry) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY, volumeConfigEntry.get() / MAXIMUM);
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
        setMessage(new TranslatableComponent("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
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

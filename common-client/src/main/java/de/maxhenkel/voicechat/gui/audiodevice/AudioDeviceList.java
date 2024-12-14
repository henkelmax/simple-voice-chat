package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;
import java.util.Collection;

public class AudioDeviceList extends ListScreenListBase<AudioDeviceEntry> {

    public static final int CELL_HEIGHT = 36;

    @Nullable
    protected ResourceLocation icon;

    @Nullable
    protected ConfigEntry<String> configEntry;

    public AudioDeviceList(int width, int height, int top) {
        super(width, height, top, CELL_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AudioDeviceEntry entry = getEntryAtPosition(mouseX, mouseY);
        if (entry == null) {
            return false;
        }
        if (!isHovered()) {
            return false;
        }
        if (!isSelected(entry.getDevice())) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            onSelect(entry);
            return true;
        }
        return false;
    }

    protected void onSelect(AudioDeviceEntry entry) {
        if (configEntry != null) {
            configEntry.set(entry.device).save();
        }
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }

    public AudioDeviceList setIcon(@Nullable ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public AudioDeviceList setConfigEntry(@Nullable ConfigEntry<String> configEntry) {
        this.configEntry = configEntry;
        return this;
    }

    @Override
    public void replaceEntries(Collection<AudioDeviceEntry> entries) {
        super.replaceEntries(entries);
    }

    public void setAudioDevices(Collection<String> entries) {
        replaceEntries(entries.stream().map(s -> new AudioDeviceEntry(s, getVisibleName(s), icon, () -> isSelected(s))).toList());
    }

    public boolean isSelected(String name) {
        if (configEntry == null) {
            return false;
        }
        return configEntry.get().equals(name);
    }

    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}

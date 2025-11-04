package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AudioDeviceList extends ListScreenListBase<AudioDeviceEntry> {

    public static final int CELL_HEIGHT = 36;

    @Nullable
    protected Identifier icon;
    @Nullable
    protected Component defaultDeviceText;

    @Nullable
    protected ConfigEntry<String> configEntry;

    public AudioDeviceList(int width, int height, int top) {
        super(width, height, top, CELL_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean bl) {
        AudioDeviceEntry entry = getEntryAtPosition(evt.x(), evt.y());
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
        return super.mouseClicked(evt, bl);
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

    @Override
    public void replaceEntries(Collection<AudioDeviceEntry> entries) {
        super.replaceEntries(entries);
    }

    public void setAudioDevices(Collection<String> entries) {
        replaceEntries(
                Stream.concat(Stream.of(""), entries.stream())
                        .map(s -> createAudioDeviceEntry(s, getVisibleName(s), icon, () -> isSelected(s)))
                        .toList()
        );
    }

    public abstract AudioDeviceEntry createAudioDeviceEntry(String device, Component name, @Nullable Identifier icon, Supplier<Boolean> isSelected);

    public boolean isSelected(String name) {
        if (configEntry == null) {
            return false;
        }
        return configEntry.get().equals(name);
    }

    public Component getVisibleName(String device) {
        if (device.isEmpty() && defaultDeviceText != null) {
            return defaultDeviceText;
        }
        return Component.literal(SoundManager.cleanDeviceName(device));
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}

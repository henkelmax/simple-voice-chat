package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;

import java.util.Collection;

public class AudioDeviceList extends ListScreenListBase<AudioDeviceEntry> {

    public AudioDeviceList(int width, int height, int top, int itemSize) {
        super(width, height, top, itemSize);
        setRenderBackground(false);
    }

    @Override
    public void replaceEntries(Collection<AudioDeviceEntry> entries) {
        super.replaceEntries(entries);
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}

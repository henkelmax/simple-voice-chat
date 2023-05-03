package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.UnregisterVolumeCategoryEvent;

public class UnregisterVolumeCategoryEventImpl extends VolumeCategoryEventImpl implements UnregisterVolumeCategoryEvent {

    public UnregisterVolumeCategoryEventImpl(VolumeCategory category) {
        super(category);
    }

}

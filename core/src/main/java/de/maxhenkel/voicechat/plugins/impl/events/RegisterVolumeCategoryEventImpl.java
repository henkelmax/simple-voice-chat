package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.RegisterVolumeCategoryEvent;

public class RegisterVolumeCategoryEventImpl extends VolumeCategoryEventImpl implements RegisterVolumeCategoryEvent {

    public RegisterVolumeCategoryEventImpl(VolumeCategory category) {
        super(category);
    }

}

package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VolumeCategory;

public interface VolumeCategoryEvent extends ServerEvent {

    /**
     * @return the volume category
     */
    VolumeCategory getVolumeCategory();

}

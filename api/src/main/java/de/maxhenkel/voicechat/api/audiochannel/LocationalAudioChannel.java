package de.maxhenkel.voicechat.api.audiochannel;

import net.minecraft.world.phys.Vec3;

public interface LocationalAudioChannel extends AudioChannel {

    /**
     * Updates the location of the audio
     *
     * @param position the audio location
     */
    void updateLocation(Vec3 position);

}

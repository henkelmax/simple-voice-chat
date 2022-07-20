package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.VolumeCategory;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ClientAudioChannel {

    /**
     * @return the ID of the channel
     */
    UUID getId();

    /**
     * Plays this audio data on this audio channel
     *
     * @param rawAudio the raw 16 bit PCM audio frame
     */
    void play(short[] rawAudio);

    /**
     * @return the category ID of the audio channel
     */
    @Nullable
    String getCategory();

    /**
     * Make sure you registered your category before using it.
     * See {@link de.maxhenkel.voicechat.api.VoicechatServerApi#registerVolumeCategory(VolumeCategory)}.
     *
     * @param category the category of the audio channel
     */
    void setCategory(@Nullable String category);

}

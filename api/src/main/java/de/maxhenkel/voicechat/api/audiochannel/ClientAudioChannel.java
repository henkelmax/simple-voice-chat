package de.maxhenkel.voicechat.api.audiochannel;

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

}

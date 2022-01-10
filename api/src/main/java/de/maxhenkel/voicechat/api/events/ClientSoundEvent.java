package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted before the client encodes the audio and sends it to the server.
 */
public interface ClientSoundEvent extends ClientEvent {

    /**
     * The unencoded audio data.
     * <br/>
     * Returns an empty array in case of the end of transmission
     *
     * @return the raw 16 bit PCM audio frame
     */
    short[] getRawAudio();

}

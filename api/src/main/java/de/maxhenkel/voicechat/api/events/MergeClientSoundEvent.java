package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted before the {@link ClientSoundEvent} is getting called.
 * It allows you to merge audio into the audio that is captured from the microphone.
 * <br/>
 * This is called even when the player is muted or not talking.
 * <br/>
 * When the voice chat it disabled, this event is not called.
 */
public interface MergeClientSoundEvent extends ClientEvent {

    /**
     * Merges the audio into the audio that is captured from the microphone.
     * <br/>
     * Calling this multiple times will merge the audio multiple times into the same audio frame.
     *
     * @param audio the audio that should get merged
     */
    void mergeAudio(short[] audio);

}

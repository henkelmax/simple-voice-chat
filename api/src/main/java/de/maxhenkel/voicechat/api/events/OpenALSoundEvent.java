package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.Position;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This event is emitted for every audio chunk for every audio channel.
 * These events are meant to be used to modify OpenAL effects.
 * <br/>
 * This specific event is emitted after the position of the sound is set, but before the any other OpenAL calls.
 * Use {@link Pre} or {@link Post} get earlier or later events respectively.
 */
public interface OpenALSoundEvent extends ClientEvent {

    /**
     * Returns the position of the sound.
     * This returns <code>null</code> for static sounds.
     *
     * @return the position of the sound
     */
    @Nullable
    Position getPosition();

    /**
     * This returns <code>null</code> for non audio channel sounds, like microphone testing.
     *
     * @return the unique ID of the audio channel
     */
    @Nullable
    UUID getChannelId();

    /**
     * @return the OpenAL source
     */
    int getSource();

    /**
     * @return the category of the sound
     */
    @Nullable
    String getCategory();

    /**
     * This event is emitted before any OpenAL calls were made by the voice chat.
     */
    public interface Pre extends OpenALSoundEvent {

    }

    /**
     * This event is emitted after all OpenAL calls by the voice chat and the audio is added to the buffer queue.
     */
    public interface Post extends OpenALSoundEvent {

    }

}

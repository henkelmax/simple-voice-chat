package de.maxhenkel.voicechat.api.packets;

import de.maxhenkel.voicechat.api.Position;

/**
 * The receiver of this event will hear the sound from the specified location.
 * If the location is further away than the maximum voice distance, the receiving player won't actually hear the sound.
 */
public interface LocationalSoundPacket extends SoundPacket {

    /**
     * @return the audio location
     */
    Position getPosition();

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * A builder to build a locational sound packet.
     *
     * <b>NOTE</b>: Some values are required to be set.
     *
     * @param <T> the builder itself
     */
    public interface Builder<T extends Builder<T>> extends SoundPacket.Builder<T, LocationalSoundPacket> {

        /**
         * This is required to be set!
         *
         * @param position the position of the sound
         * @return the builder
         */
        T position(Position position);

        /**
         * @param distance the distance, this packet can be heard
         * @return the builder
         */
        T distance(float distance);

    }

}

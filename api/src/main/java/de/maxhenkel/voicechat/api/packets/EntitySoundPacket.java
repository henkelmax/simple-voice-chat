package de.maxhenkel.voicechat.api.packets;

import java.util.UUID;

/**
 * The receiver of this event will hear the sound from the specified entity.
 * This can be done for all entities, but only players will show the speaker icon.
 * If the entity is further away than the maximum voice distance, the receiving player won't actually hear the sound.
 */
public interface EntitySoundPacket extends SoundPacket {

    /**
     * @return the UUID of the entity
     */
    UUID getEntityUuid();

    /**
     * @return if the entity is whispering
     */
    boolean isWhispering();

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * A builder to build an entity sound packet.
     *
     * <b>NOTE</b>: Some values are required to be set.
     *
     * @param <T> the builder itself
     */
    public interface Builder<T extends Builder<T>> extends SoundPacket.Builder<T, EntitySoundPacket> {

        /**
         * This is required to be set!
         *
         * @param entityUuid the UUID of the entity
         * @return the builder
         */
        T entityUuid(UUID entityUuid);

        /**
         * @param whispering if the entity should be whispering
         * @return the builder
         */
        T whispering(boolean whispering);

        /**
         * @param distance the distance, this packet can be heard
         * @return the builder
         */
        T distance(float distance);

    }

}

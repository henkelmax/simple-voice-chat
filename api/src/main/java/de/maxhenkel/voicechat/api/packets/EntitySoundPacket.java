package de.maxhenkel.voicechat.api.packets;

import java.util.UUID;

/**
 * The receiver of this event will hear the sound from the specified entity
 * This can be done for all entities, but only players will show the speaker icon
 * If the entity is further away than the maximum voice distance, the receiving player won't actually hear the sound
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

}

package de.maxhenkel.voicechat.api.packets;

import de.maxhenkel.voicechat.api.Position;

import java.util.UUID;

public interface ConvertablePacket {

    /**
     * Converts this packet to an entity sound packet.
     *
     * @param entityUuid the UUID of the entity
     * @param whispering if the entity is whispering
     * @return the entity sound packet
     */
    EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering);

    /**
     * Converts this packet to a locational sound packet.
     *
     * @param position the position of the audio
     * @return the locational sound packet
     */
    LocationalSoundPacket toLocationalSoundPacket(Position position);

    /**
     * Converts this packet to a static sound packet.
     *
     * @return the static sound packet
     */
    StaticSoundPacket toStaticSoundPacket();

}

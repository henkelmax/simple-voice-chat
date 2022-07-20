package de.maxhenkel.voicechat.api.packets;

import de.maxhenkel.voicechat.api.Position;

import java.util.UUID;

public interface ConvertablePacket {

    /**
     * @return a builder to build an entity sound packet based on this packet
     */
    EntitySoundPacket.Builder<?> entitySoundPacketBuilder();

    /**
     * @return a builder to build a locational sound packet based on this packet
     */
    LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder();

    /**
     * @return a builder to build a static sound packet based on this packet
     */
    StaticSoundPacket.Builder<?> staticSoundPacketBuilder();

    /**
     * Converts this packet to an entity sound packet.
     *
     * @param entityUuid the UUID of the entity
     * @param whispering if the entity is whispering
     * @return the entity sound packet
     * @deprecated use {@link #entitySoundPacketBuilder()}
     */
    @Deprecated
    EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering);

    /**
     * Converts this packet to a locational sound packet.
     *
     * @param position the position of the audio
     * @return the locational sound packet
     * @deprecated use {@link #locationalSoundPacketBuilder()}
     */
    @Deprecated
    LocationalSoundPacket toLocationalSoundPacket(Position position);

    /**
     * Converts this packet to a static sound packet.
     *
     * @return the static sound packet
     * @deprecated use {@link #staticSoundPacketBuilder()}
     */
    @Deprecated
    StaticSoundPacket toStaticSoundPacket();

}

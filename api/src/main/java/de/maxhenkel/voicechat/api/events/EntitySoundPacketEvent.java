package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;

/**
 * This event is emitted when an entity sound packet is about to get sent to a client.
 */
public interface EntitySoundPacketEvent extends SoundPacketEvent<EntitySoundPacket> {

}

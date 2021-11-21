package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;

/**
 * This event is emitted when a locational sound packet is about to get sent to a client.
 */
public interface LocationalSoundPacketEvent extends SoundPacketEvent<LocationalSoundPacket> {

}

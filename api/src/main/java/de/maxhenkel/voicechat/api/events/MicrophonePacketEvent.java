package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

/**
 * This event is emitted when a microphone packet arrives at the server.
 */
public interface MicrophonePacketEvent extends PacketEvent<MicrophonePacket> {

}

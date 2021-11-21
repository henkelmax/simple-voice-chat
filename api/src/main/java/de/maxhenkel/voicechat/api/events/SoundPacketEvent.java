package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.packets.Packet;

public interface SoundPacketEvent<T extends Packet> extends PacketEvent<T> {

    String SOURCE_GROUP = "group";
    String SOURCE_PROXIMITY = "proximity";
    String SOURCE_SPECTATOR = "spectator";
    String SOURCE_PLUGIN = "plugin";

    /**
     * Where the packet originated from.
     * It can be either {@value SOURCE_GROUP} for group chats,
     * {@value SOURCE_PROXIMITY} for proximity sound,
     * {@value SOURCE_SPECTATOR} for spectator sound
     * or {@value SOURCE_PLUGIN} if the packet was sent from a plugin.
     *
     * @return the source of the packet
     */
    String getSource();

}

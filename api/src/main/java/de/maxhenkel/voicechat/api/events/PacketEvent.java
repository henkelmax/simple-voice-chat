package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.packets.Packet;

import javax.annotation.Nullable;

public interface PacketEvent<T extends Packet> extends ServerEvent {

    /**
     * @return the packet
     */
    T getPacket();

    /**
     * @return the connection of the player that should receive this packet
     */
    @Nullable
    VoicechatConnection getReceiverConnection();

    /**
     * @return the connection of the player that sent this packet or <code>null</code> if it wasn't sent by a player
     */
    @Nullable
    VoicechatConnection getSenderConnection();

}
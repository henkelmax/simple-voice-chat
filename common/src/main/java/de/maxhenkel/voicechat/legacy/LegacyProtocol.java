package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.server.ConnectionSecret;
import de.maxhenkel.voicechat.voice.server.Server;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.UUID;

public interface LegacyProtocol {

    @Nullable
    Packet<?> translateOutgoing(Packet<?> packet);

    @Nullable
    <T extends Packet<T>> T translateIncoming(T packet);

    /**
     * @param packet the raw voice chat packet
     * @return the UUID of the player that sent the packet or <code>null</code> if it's not in the format of this protocol
     */
    @Nullable
    UUID getPlayerUuid(byte[] packet) throws Exception;

    /**
     * Gets called after determining with {@link #getPlayerUuid(byte[])}.
     *
     * @param packet the raw voice chat packet in the format of this protocol
     * @return the packet in the current format
     */
    byte[] translateIncomingVoicePacket(Server server, ConnectionSecret connectionSecret, SocketAddress address, byte[] packet) throws Exception;

    /**
     * @param packet a raw voice chat packet in the current format
     * @return the packet in the format of this protocol
     */
    byte[] translateOutgoingVoicePacket(ConnectionSecret connectionSecret, byte[] packet) throws Exception;

}

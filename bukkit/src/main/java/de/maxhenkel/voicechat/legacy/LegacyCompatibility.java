package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.ConnectionSecret;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.UdpPacket;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LegacyCompatibility {

    private static final Map<Integer, LegacyProtocol> PROTOCOLS;

    static {
        PROTOCOLS = new HashMap<>();
        PROTOCOLS.put(18, new LegacyProtocolV18());
        PROTOCOLS.put(20, new LegacyProtocolV20());
    }

    public static boolean isEnabled() {
        return System.getProperty("voicechat.legacy") != null;
    }

    public static boolean isSupported(int compatibilityVersion) {
        return isEnabled() && PROTOCOLS.containsKey(compatibilityVersion);
    }

    /**
     * @return <code>null</code> in case there's no translation needed or available
     */
    @Nullable
    public static LegacyProtocol getProtocol(UUID playerUuid) {
        if (!isEnabled()) {
            return null;
        }
        return PROTOCOLS.get(Voicechat.SERVER.getCompatibilityVersion(playerUuid));
    }

    /**
     * Registers the channels that only exist in legacy protocols.
     * Only register once as this is shared between all protocols.
     */
    public static void registerChannels(NetManager netManager) throws Exception {
        if (!isEnabled()) {
            return;
        }
        netManager.registerIncomingPacket(LegacyUpdateStatePacket.class);
        netManager.registerOutgoingPacket(LegacyPlayerStatePacket.class);
        netManager.registerOutgoingPacket(LegacyPlayerStatesPacket.class);
    }

    /**
     * @return <code>null</code> if the client doesn't support it
     */
    @Nullable
    public static Packet<?> translateOutgoing(Player player, Packet<?> packet) {
        LegacyProtocol protocol = getProtocol(player.getUniqueId());
        if (protocol == null) {
            return packet;
        }
        return protocol.translateOutgoing(packet);
    }

    /**
     * @return <code>null</code> if the packet should be dropped
     */
    @Nullable
    public static <T extends Packet<?>> T translateIncoming(Player player, T packet) {
        LegacyProtocol protocol = getProtocol(player.getUniqueId());
        if (protocol == null) {
            return packet;
        }
        return protocol.translateIncoming(packet);
    }

    /**
     * @return the packet in the current format
     */
    public static UdpPacket translateIncomingVoicePacket(Server server, UdpPacket packet) {
        if (!isEnabled()) {
            return packet;
        }
        for (LegacyProtocol protocol : PROTOCOLS.values()) {
            try {
                UUID playerUuid = protocol.getPlayerUuid(packet.getData());
                if (playerUuid == null || getProtocol(playerUuid) != protocol) {
                    continue;
                }
                ConnectionSecret connectionSecret = server.getConnectionSecrets().get(playerUuid);
                if (connectionSecret == null) {
                    continue;
                }
                byte[] translated = protocol.translateIncomingVoicePacket(server, connectionSecret, packet.getSocketAddress(), packet.getData());
                return new UdpPacket(translated, packet.getSocketAddress(), packet.getTimestamp());
            } catch (Exception e) {
                Voicechat.LOGGER.debug("Failed to translate voice chat packet from {}", packet.getSocketAddress(), e);
            }
        }
        return packet;
    }

    /**
     * @return the packet in the format of the client
     */
    public static byte[] translateOutgoingVoicePacket(ClientConnection connection, NetworkMessage message) throws Exception {
        LegacyProtocol protocol = getProtocol(connection.getPlayerUUID());
        if (protocol == null) {
            return message.writeServer(connection);
        }
        return protocol.translateOutgoingVoicePacket(connection, message);
    }

}

package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.network.VoiceProxyServer;
import de.maxhenkel.voicechat.util.ByteBufferWrapper;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PingHandler {

    public static final UUID PING_V1 = UUID.fromString("58bc9ae9-c7a8-45e4-a11c-efbb67199425");

    public static boolean onPacket(VoiceProxyServer proxy, SocketAddress socketAddress, UUID playerID, ByteBuffer buf) {
        if (!proxy.getVoiceProxy().getConfig().allowPings.get()) {
            return false;
        }
        if (!PING_V1.equals(playerID)) {
            return false;
        }
        try {
            ByteBufferWrapper b = new ByteBufferWrapper(buf);
            byte[] payload = b.readByteArray();
            ByteBufferWrapper buffer = new ByteBufferWrapper(ByteBuffer.wrap(payload));
            UUID id = buffer.readUUID();
            long timestamp = buffer.readLong();
            proxy.getVoiceProxy().getLogger().debug("Received ping {} from {}", id, socketAddress);
            ByteBufferWrapper responseBuffer = new ByteBufferWrapper(ByteBuffer.allocate((128 + 64)));

            responseBuffer.writeUUID(id);
            responseBuffer.writeLong(timestamp);

            byte[] bytes = responseBuffer.toBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            packet.setSocketAddress(socketAddress);
            proxy.write(packet);
        } catch (Exception e) {
            proxy.getVoiceProxy().getLogger().debug("Failed to send ping to {}: {}", socketAddress, e.getMessage());
        }
        return true;
    }
}

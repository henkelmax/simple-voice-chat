package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.SocketAddress;
import java.util.UUID;

public class PingHandler {

    public static final UUID PING_V1 = UUID.fromString("58bc9ae9-c7a8-45e4-a11c-efbb67199425");

    public static boolean onPacket(Server server, SocketAddress socketAddress, UUID playerID, ByteBuf buf) {
        if (!Voicechat.SERVER_CONFIG.allowPings.get()) {
            return false;
        }
        if (!PING_V1.equals(playerID)) {
            return false;
        }
        try {
            byte[] payload = BufferUtils.readByteArray(buf);
            ByteBuf buffer = Unpooled.wrappedBuffer(payload);
            UUID id = BufferUtils.readUUID(buffer);
            long timestamp = buffer.readLong();
            Voicechat.LOGGER.debug("Received ping {} from {}", id, socketAddress);

            ByteBuf responseBuffer = Unpooled.buffer(24);

            BufferUtils.writeUUID(responseBuffer, id);
            responseBuffer.writeLong(timestamp);

            byte[] response = new byte[responseBuffer.readableBytes()];
            responseBuffer.readBytes(response);

            server.getSocket().send(response, socketAddress);
        } catch (Exception e) {
            Voicechat.LOGGER.debug("Failed to send ping to {}: {}", socketAddress, e.getMessage());
        }
        return true;
    }
}

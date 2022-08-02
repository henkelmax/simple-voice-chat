package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

    public static final byte MAGIC_BYTE = (byte) 0b11111111;

    private final long timestamp;
    private Packet<? extends Packet> packet;
    private SocketAddress address;

    public NetworkMessage(long timestamp, Packet<?> packet) {
        this(timestamp);
        this.packet = packet;
    }

    public NetworkMessage(Packet<?> packet) {
        this(System.currentTimeMillis());
        this.packet = packet;
    }

    private NetworkMessage(long timestamp) {
        this.timestamp = timestamp;
    }

    @Nonnull
    public Packet<? extends Packet> getPacket() {
        return packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTTL() {
        return packet.getTTL();
    }

    public SocketAddress getAddress() {
        return address;
    }

    private static final Map<Byte, Class<? extends Packet>> packetRegistry;

    static {
        packetRegistry = new HashMap<>();
        packetRegistry.put((byte) 0x1, MicPacket.class);
        packetRegistry.put((byte) 0x2, PlayerSoundPacket.class);
        packetRegistry.put((byte) 0x3, GroupSoundPacket.class);
        packetRegistry.put((byte) 0x4, LocationSoundPacket.class);
        packetRegistry.put((byte) 0x5, AuthenticatePacket.class);
        packetRegistry.put((byte) 0x6, AuthenticateAckPacket.class);
        packetRegistry.put((byte) 0x7, PingPacket.class);
        packetRegistry.put((byte) 0x8, KeepAlivePacket.class);
    }

    @Nullable
    public static NetworkMessage readPacketServer(RawUdpPacket packet, Server server) throws IllegalAccessException, InstantiationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvocationTargetException, NoSuchMethodException {
        byte[] data = packet.getData();
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        if (b.readByte() != MAGIC_BYTE) {
            Voicechat.logDebug("Received invalid packet from {}", packet.getSocketAddress());
            return null;
        }
        UUID playerID = b.readUUID();
        if (!server.hasSecret(playerID)) {
            // Ignore packets if they are not from a player that has a secret
            Voicechat.logDebug("Player {} does not have a secret", playerID);
            return null;
        }
        return readFromBytes(packet.getSocketAddress(), server.getSecret(playerID), b.readByteArray(), packet.getTimestamp());
    }

    @Nullable
    private static NetworkMessage readFromBytes(SocketAddress socketAddress, UUID secret, byte[] encryptedPayload, long timestamp) throws InstantiationException, IllegalAccessException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchMethodException, InvocationTargetException {
        byte[] decrypt;
        try {
            decrypt = AES.decrypt(secret, encryptedPayload);
        } catch (Exception e) {
            // Return null if the encryption fails due to a wrong secret
            Voicechat.logDebug("Failed to decrypt packet from {}", socketAddress);
            return null;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(decrypt));
        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            Voicechat.logDebug("Got invalid packet ID {}", packetType);
            return null;
        }
        Packet<? extends Packet<?>> p = packetClass.getDeclaredConstructor().newInstance();

        NetworkMessage message = new NetworkMessage(timestamp);
        message.address = socketAddress;
        message.packet = p.fromBytes(buffer);

        return message;
    }

    public UUID getSender(Server server) {
        return server.getConnections().values().stream().filter(connection -> connection.getAddress().equals(address)).map(ClientConnection::getPlayerUUID).findAny().orElse(null);
    }

    private static byte getPacketType(Packet<? extends Packet> packet) {
        for (Map.Entry<Byte, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            if (packet.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public byte[] writeServer(Server server, ClientConnection connection) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] payload = write(server.getSecret(connection.getPlayerUUID()));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(1 + payload.length));
        buffer.writeByte(MAGIC_BYTE);
        buffer.writeByteArray(payload);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    public byte[] write(UUID secret) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        byte type = getPacketType(packet);
        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);
        packet.toBytes(buffer);

        return AES.encrypt(secret, buffer.array());
    }

}

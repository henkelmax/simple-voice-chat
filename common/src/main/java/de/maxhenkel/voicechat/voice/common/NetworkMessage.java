package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

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

    public static NetworkMessage readPacketClient(DatagramSocket socket, ClientVoicechatConnection client) throws IllegalAccessException, InstantiationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvocationTargetException, NoSuchMethodException {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return readFromBytes(packet.getSocketAddress(), client.getData().getSecret(), data, System.currentTimeMillis());
    }

    public static NetworkMessage readPacketServer(RawUdpPacket packet, Server server) throws IllegalAccessException, InstantiationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvocationTargetException, NoSuchMethodException {
        byte[] data = packet.getData();
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        UUID playerID = b.readUUID();
        if (!server.hasSecret(playerID)) {
            throw new InvalidKeyException("Player %s does not have a secret".formatted(playerID));
        }
        return readFromBytes(packet.getSocketAddress(), server.getSecret(playerID), b.readByteArray(), packet.getTimestamp());
    }

    private static NetworkMessage readFromBytes(SocketAddress socketAddress, UUID secret, byte[] encryptedPayload, long timestamp) throws InstantiationException, IllegalAccessException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchMethodException, InvocationTargetException {
        byte[] decrypt = AES.decrypt(secret, encryptedPayload);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(decrypt));
        UUID readSecret = buffer.readUUID();

        if (!secret.equals(readSecret)) {
            throw new InvalidKeyException("Secrets do not match");
        }

        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID %s".formatted(packetType));
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

    public byte[] writeClient(ClientVoicechatConnection client) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] payload = write(client.getData().getSecret());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(payload.length + 16));
        buffer.writeUUID(client.getData().getPlayerUUID());
        buffer.writeByteArray(payload);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    public byte[] write(UUID secret) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeUUID(secret);

        byte type = getPacketType(packet);
        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);
        packet.toBytes(buffer);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return AES.encrypt(secret, bytes);
    }

}

package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
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

    public NetworkMessage(Packet<?> packet) {
        this();
        this.packet = packet;
    }

    private NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
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
        packetRegistry.put((byte) 0, MicPacket.class);
        packetRegistry.put((byte) 1, SoundPacket.class);
        packetRegistry.put((byte) 2, AuthenticatePacket.class);
        packetRegistry.put((byte) 3, AuthenticateAckPacket.class);
        packetRegistry.put((byte) 4, PingPacket.class);
        packetRegistry.put((byte) 5, KeepAlivePacket.class);
    }

    public static NetworkMessage readPacketClient(DatagramSocket socket, Client client) throws IllegalAccessException, InstantiationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return readFromBytes(packet.getSocketAddress(), client.getSecret(), data);
    }

    public static NetworkMessage readPacketServer(DatagramSocket socket, Server server) throws IllegalAccessException, InstantiationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        PacketBuffer b = new PacketBuffer(Unpooled.wrappedBuffer(data));
        UUID playerID = b.readUUID();
        return readFromBytes(packet.getSocketAddress(), server.getSecret(playerID), b.readByteArray());
    }

    private static NetworkMessage readFromBytes(SocketAddress socketAddress, UUID secret, byte[] encryptedPayload) throws InstantiationException, IllegalAccessException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] decrypt = AES.decrypt(secret, encryptedPayload);
        PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(decrypt));
        UUID readSecret = buffer.readUUID();

        if (!secret.equals(readSecret)) {
            throw new InvalidKeyException("Secrets do not match");
        }

        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID " + packetType);
        }
        Packet<? extends Packet<?>> p = packetClass.newInstance();

        NetworkMessage message = new NetworkMessage();
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

    public byte[] writeClient(Client client) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] payload = write(client.getSecret());
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(payload.length + 32));
        buffer.writeUUID(client.getPlayerUUID());
        buffer.writeByteArray(payload);
        return buffer.array();
    }

    public byte[] write(UUID secret) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeUUID(secret);

        byte type = getPacketType(packet);
        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);
        packet.toBytes(buffer);

        return AES.encrypt(secret, buffer.array());
    }

}

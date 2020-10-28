package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

    private Packet<? extends Packet> packet;
    private UUID playerUUID;
    private UUID secret;
    private InetAddress address;
    private int port;
    private long timestamp;
    private long ttl;

    public NetworkMessage(Packet<?> packet, UUID playerUUID, UUID secret, InetAddress address, int port) {
        this();
        this.packet = packet;
        this.playerUUID = playerUUID;
        this.secret = secret;
        this.address = address;
        this.port = port;
    }

    public NetworkMessage(Packet<?> packet, UUID playerUUID, UUID secret) {
        this(packet, playerUUID, secret, null, -1);
    }

    private NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
        this.ttl = 2000L;
    }

    @Nonnull
    public Packet<? extends Packet> getPacket() {
        return packet;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTTL() {
        return ttl;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    private static final Map<Byte, Class<? extends Packet>> packetRegistry;

    static {
        packetRegistry = new HashMap<>();
        packetRegistry.put((byte) 0, SoundPacket.class);
        packetRegistry.put((byte) 1, AuthenticatePacket.class);
        packetRegistry.put((byte) 2, AuthenticateAckPacket.class);
    }

    public static NetworkMessage readPacket(DatagramSocket socket) throws IllegalAccessException, InstantiationException, IOException {
        DatagramPacket packet = new DatagramPacket(new byte[10_000], 10_000); //TODO better size
        socket.receive(packet);
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

        PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(data));
        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID " + packetType);
        }
        Packet<? extends Packet<?>> p = packetClass.newInstance();

        NetworkMessage message = new NetworkMessage();
        message.playerUUID = buffer.readUniqueId();
        message.secret = buffer.readUniqueId();
        message.address = packet.getAddress();
        message.port = packet.getPort();
        message.packet = p.fromBytes(buffer);

        return message;
    }

    private static byte getPacketType(Packet<? extends Packet> packet) {
        for (Map.Entry<Byte, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            if (packet.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public byte[] write() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

        byte type = getPacketType(packet);

        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);
        buffer.writeUniqueId(playerUUID);
        buffer.writeUniqueId(secret);
        packet.toBytes(buffer);

        return buffer.array();
    }

    public void sendToServer(Client client) throws IOException {
        byte[] data = write();
        client.getSocket().send(new DatagramPacket(data, data.length, client.getAddress(), client.getPort()));
    }

    public void sendTo(DatagramSocket socket, ClientConnection connection) throws IOException {
        byte[] data = write();
        socket.send(new DatagramPacket(data, data.length, connection.getAddress(), connection.getPort()));
    }

}

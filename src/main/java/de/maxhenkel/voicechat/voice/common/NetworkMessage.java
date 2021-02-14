package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

    private final long timestamp;
    private final long ttl;
    private Packet<? extends Packet> packet;
    private UUID secret;
    private SocketAddress address;

    public NetworkMessage(Packet<?> packet, UUID secret) {
        this(packet);
        this.secret = secret;
    }

    public NetworkMessage(Packet<?> packet) {
        this();
        this.packet = packet;
    }

    private NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
        this.ttl = 2000L;
    }

    @Nonnull
    public Packet<? extends Packet> getPacket() {
        return packet;
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
    }

    public static NetworkMessage readPacket(DatagramSocket socket) throws IllegalAccessException, InstantiationException, IOException {
        // 4096 is the maximum packet size a packet can have with 44100 hz
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.wrappedBuffer(data));
        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID " + packetType);
        }
        Packet<? extends Packet<?>> p = packetClass.newInstance();

        NetworkMessage message = new NetworkMessage();
        if (buffer.readBoolean()) {
            message.secret = buffer.readUuid();
        }
        message.address = packet.getSocketAddress();
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

    public byte[] write() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        byte type = getPacketType(packet);

        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);
        buffer.writeBoolean(secret != null);
        if (secret != null) {
            buffer.writeUuid(secret);
        }
        packet.toBytes(buffer);

        return buffer.array();
    }

    public void sendToServer(Client client) throws IOException {
        byte[] data = write();
        client.getSocket().send(new DatagramPacket(data, data.length, client.getAddress(), client.getPort()));
    }

    public void sendTo(DatagramSocket socket, ClientConnection connection) throws IOException {
        byte[] data = write();
        socket.send(new DatagramPacket(data, data.length, connection.getAddress()));
    }

}

package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

    private Packet<? extends Packet> packet;
    private UUID playerUUID;

    public NetworkMessage(Packet<?> packet, UUID playerUUID) {
        this.packet = packet;
        this.playerUUID = playerUUID;
    }

    public NetworkMessage(Packet<?> packet) {
        this.packet = packet;
    }

    private NetworkMessage() {

    }

    @Nonnull
    public Packet<? extends Packet> getPacket() {
        return packet;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    private static final Map<Byte, Class<? extends Packet>> packetRegistry;

    static {
        packetRegistry = new HashMap<>();
        packetRegistry.put((byte) 0, SoundPacket.class);
        packetRegistry.put((byte) 1, AuthenticatePacket.class);
        packetRegistry.put((byte) 2, KeepAlivePacket.class);
    }

    public static NetworkMessage readPacket(DataInputStream inputStream) throws IOException, IllegalAccessException, InstantiationException {
        int length = Math.max(0, inputStream.readInt());
        byte[] compressedData = new byte[length];
        inputStream.readFully(compressedData);
        byte[] data = Utils.gUnzip(compressedData);
        PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(data));
        byte packetType = buffer.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            packetClass = KeepAlivePacket.class;
        }
        Packet<? extends Packet<?>> p = packetClass.newInstance();

        NetworkMessage message = new NetworkMessage();
        if (buffer.readBoolean()) {
            message.playerUUID = buffer.readUniqueId();
        }

        message.packet = p.fromBytes(buffer);

        return message;
    }

    public static NetworkMessage readPacket(DataInputStream inputStream, UUID playerUUID) throws IOException, IllegalAccessException, InstantiationException {
        NetworkMessage networkMessage = readPacket(inputStream);
        networkMessage.playerUUID = playerUUID;
        return networkMessage;
    }

    private static byte getPacketType(Packet<? extends Packet> packet) {
        for (Map.Entry<Byte, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            if (packet.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void send(DataOutputStream outputStream) throws IOException {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

        byte type = getPacketType(packet);

        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        buffer.writeByte(type);

        if (playerUUID != null) {
            buffer.writeBoolean(true);
            buffer.writeUniqueId(playerUUID);
        } else {
            buffer.writeBoolean(false);
        }

        packet.toBytes(buffer);

        byte[] compressedData = Utils.gzip(buffer.array());

        outputStream.writeInt(compressedData.length);
        outputStream.write(compressedData);
        outputStream.flush();
    }
}

package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.plugins.impl.RawUdpPacketImpl;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UDPWrapperPacket implements Packet<UDPWrapperPacket> {

    public static final Key DATA = Voicechat.compatibility.createNamespacedKey("data");

    private List<RawUdpPacket> packets;
    private UUID sender;

    public UDPWrapperPacket(List<RawUdpPacket> packets, UUID sender) {
        this.packets = packets;
        this.sender = sender;
    }

    public UDPWrapperPacket(List<RawUdpPacket> packets) {
        this.packets = packets;
        this.sender = new UUID(0L, 0L);
    }

    public UDPWrapperPacket() {

    }

    public List<RawUdpPacket> getPackets() {
        return packets;
    }

    @Override
    public Key getID() {
        return DATA;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.onUDPWrapperPacket(this);
    }

    @Override
    public UDPWrapperPacket fromBytes(FriendlyByteBuf buf) {
        sender = buf.readUUID();
        int size = buf.readInt();
        List<RawUdpPacket> readPackets = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            byte[] data = new byte[buf.readInt()];
            buf.readBytes(data);
            RawUdpPacketImpl packet = new RawUdpPacketImpl(data, new IntegratedSocketAddress(sender), System.currentTimeMillis());
            readPackets.add(packet);
        }
        packets = readPackets;
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeInt(packets.size());
        for (RawUdpPacket packet : packets) {
            byte[] data = packet.getData();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
    }
}

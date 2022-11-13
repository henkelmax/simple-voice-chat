package de.maxhenkel.voicechat.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeNetworkEvents {

    private static final Map<String, ServerCustomPayloadEvent> serverPackets = new ConcurrentHashMap<>();
    private static final Map<String, ClientCustomPayloadEvent> clientPackets = new ConcurrentHashMap<>();

    public static void registerServerPacket(ResourceLocation channel, ServerCustomPayloadEvent event) {
        serverPackets.put(channel.toString(), event);
    }

    public static void registerClientPacket(ResourceLocation channel, ClientCustomPayloadEvent event) {
        clientPackets.put(channel.toString(), event);
    }

    public static boolean onCustomPayloadServer(CPacketCustomPayload packet, EntityPlayerMP player) {
        ServerCustomPayloadEvent event = serverPackets.get(packet.getChannelName());
        if (event != null) {
            event.onCustomPayload(packet, player);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static boolean onCustomPayloadClient(SPacketCustomPayload packet) {
        ClientCustomPayloadEvent event = clientPackets.get(packet.getChannelName());
        if (event != null) {
            event.onCustomPayload(packet);
            return true;
        }
        return false;
    }

    public interface ServerCustomPayloadEvent {
        void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player);
    }

    public interface ClientCustomPayloadEvent {
        void onCustomPayload(SPacketCustomPayload packet);
    }

}

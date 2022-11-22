package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.HashSet;
import java.util.Set;

public class QuiltNetManager extends NetManager {

    private final Set<ResourceLocation> packets;

    public QuiltNetManager() {
        packets = new HashSet<>();
    }

    public Set<ResourceLocation> getPackets() {
        return packets;
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            ResourceLocation identifier = dummyPacket.getIdentifier();
            packets.add(identifier);
            if (toServer) {
                ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buf, responseSender) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        c.onServerPacket(server, player, handler, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (toClient && !CommonCompatibilityManager.INSTANCE.isDedicatedServer()) {
                ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, buf, responseSender) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        client.execute(() -> c.onClientPacket(client, handler, packet));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

}

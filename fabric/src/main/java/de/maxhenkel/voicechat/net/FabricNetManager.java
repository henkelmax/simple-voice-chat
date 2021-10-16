package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class FabricNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            if (toServer) {
                ServerPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (server, player, handler, buf, responseSender) -> {
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
            if (toClient && FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
                ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (client, handler, buf, responseSender) -> {
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

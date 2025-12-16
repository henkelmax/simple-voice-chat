package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.MinecraftCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public class ForgeNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        ClientServerChannel<T> c = new ClientServerChannel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            EventNetworkChannel channel = NetworkRegistry.newEventChannel(
                    new ResourceLocation(Voicechat.MODID, dummyPacket.getID()),
                    () -> NetworkRegistry.ACCEPTVANILLA,
                    NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA),
                    NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA)
            );
            channel.addListener(event -> {
                if (event.getPayload() == null) {
                    return;
                }
                NetworkEvent.Context context = event.getSource().get();
                if (toServer && context.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
                    try {
                        if (!Voicechat.SERVER.isCompatible(MinecraftCompatibilityManager.fromServerPlayer(context.getSender())) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        c.onServerPacket(MinecraftCompatibilityManager.fromServer(context.getSender().server), MinecraftCompatibilityManager.fromServerPlayer(context.getSender()), context.getSender().connection, packet);
                        context.setPacketHandled(true);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                } else {
                    if (isSameThread()) {
                        return;
                    }
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        onClientPacket(c, packet);
                        context.setPacketHandled(true);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(ClientServerChannel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isSameThread() {
        return Minecraft.getInstance().isSameThread();
    }

}

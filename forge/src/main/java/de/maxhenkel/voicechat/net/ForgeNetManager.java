package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ForgeNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();

            if (toServer) {
                ForgeNetworkEvents.registerServerPacket(dummyPacket.getIdentifier(), (packet, player) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T vcPacket = packetType.getDeclaredConstructor().newInstance();
                        vcPacket.fromBytes(packet.getBufferData());
                        c.onServerPacket(player.mcServer, player, player.connection, vcPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            if (toClient) {
                ForgeNetworkEvents.registerClientPacket(dummyPacket.getIdentifier(), payload -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(payload.getBufferData());
                        onClientPacket(c, packet);
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

    @SideOnly(Side.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getMinecraft(), Minecraft.getMinecraft().getConnection(), packet);
    }

}

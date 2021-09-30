package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public abstract class NetManager {

    public Channel<PlayerStatePacket> playerStateChannel;
    public Channel<PlayerStatesPacket> playerStatesChannel;
    public Channel<SecretPacket> secretChannel;
    public Channel<SetGroupPacket> setGroupChannel;
    public Channel<RequestSecretPacket> requestSecretChannel;

    public void init() {
        playerStateChannel = registerReceiver(PlayerStatePacket.class, true, true);
        playerStatesChannel = registerReceiver(PlayerStatesPacket.class, true, false);
        secretChannel = registerReceiver(SecretPacket.class, true, false);
        setGroupChannel = registerReceiver(SetGroupPacket.class, true, false);
        requestSecretChannel = registerReceiver(RequestSecretPacket.class, false, true);
    }

    public abstract <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer);

    public abstract void sendToServer(Packet<?> packet);

    public abstract void sendToClient(ServerPlayer player, Packet<?> packet);

    public interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet);
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPacketListener handler, T packet);
    }

}

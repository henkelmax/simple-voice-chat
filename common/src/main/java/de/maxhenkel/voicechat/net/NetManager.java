package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.server.MinecraftServer;

public abstract class NetManager {

    public Channel<UpdateStatePacket> updateStateChannel;
    public Channel<PlayerStatePacket> playerStateChannel;
    public Channel<PlayerStatesPacket> playerStatesChannel;
    public Channel<SecretPacket> secretChannel;
    public Channel<RequestSecretPacket> requestSecretChannel;
    public Channel<JoinGroupPacket> joinGroupChannel;
    public Channel<CreateGroupPacket> createGroupChannel;
    public Channel<LeaveGroupPacket> leaveGroupChannel;
    public Channel<JoinedGroupPacket> joinedGroupChannel;

    public void init() {
        updateStateChannel = registerReceiver(UpdateStatePacket.class, false, true);
        playerStateChannel = registerReceiver(PlayerStatePacket.class, true, false);
        playerStatesChannel = registerReceiver(PlayerStatesPacket.class, true, false);
        secretChannel = registerReceiver(SecretPacket.class, true, false);
        requestSecretChannel = registerReceiver(RequestSecretPacket.class, false, true);
        joinGroupChannel = registerReceiver(JoinGroupPacket.class, false, true);
        createGroupChannel = registerReceiver(CreateGroupPacket.class, false, true);
        leaveGroupChannel = registerReceiver(LeaveGroupPacket.class, false, true);
        joinedGroupChannel = registerReceiver(JoinedGroupPacket.class, true, false);
    }

    public abstract <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer);

    public static void sendToServer(Packet<?> packet) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new CCustomPayloadPacket(packet.getIdentifier(), buffer));
        }
    }

    public static void sendToClient(ServerPlayerEntity player, Packet<?> packet) {
        if (!Voicechat.SERVER.isCompatible(player)) {
            return;
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        packet.toBytes(buffer);
        player.connection.send(new SCustomPayloadPlayPacket(packet.getIdentifier(), buffer));
    }

    public interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, T packet);
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPlayNetHandler handler, T packet);
    }

}

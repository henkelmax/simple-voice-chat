package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public abstract class NetManager {

    public Channel<UpdateStatePacket> updateStateChannel;
    public Channel<PlayerStatePacket> playerStateChannel;
    public Channel<PlayerStatesPacket> playerStatesChannel;
    public Channel<SecretPacket> secretChannel;
    public Channel<RequestSecretPacket> requestSecretChannel;
    public Channel<AddGroupPacket> addGroupChannel;
    public Channel<RemoveGroupPacket> removeGroupChannel;
    public Channel<JoinGroupPacket> joinGroupChannel;
    public Channel<CreateGroupPacket> createGroupChannel;
    public Channel<LeaveGroupPacket> leaveGroupChannel;
    public Channel<JoinedGroupPacket> joinedGroupChannel;
    public Channel<AddCategoryPacket> addCategoryChannel;
    public Channel<RemoveCategoryPacket> removeCategoryChannel;

    public void init() {
        updateStateChannel = registerReceiver(UpdateStatePacket.class, false, true);
        playerStateChannel = registerReceiver(PlayerStatePacket.class, true, false);
        playerStatesChannel = registerReceiver(PlayerStatesPacket.class, true, false);
        secretChannel = registerReceiver(SecretPacket.class, true, false);
        requestSecretChannel = registerReceiver(RequestSecretPacket.class, false, true);
        addGroupChannel = registerReceiver(AddGroupPacket.class, true, false);
        removeGroupChannel = registerReceiver(RemoveGroupPacket.class, true, false);
        joinGroupChannel = registerReceiver(JoinGroupPacket.class, false, true);
        createGroupChannel = registerReceiver(CreateGroupPacket.class, false, true);
        leaveGroupChannel = registerReceiver(LeaveGroupPacket.class, false, true);
        joinedGroupChannel = registerReceiver(JoinedGroupPacket.class, true, false);
        addCategoryChannel = registerReceiver(AddCategoryPacket.class, true, false);
        removeCategoryChannel = registerReceiver(RemoveCategoryPacket.class, true, false);
    }

    public abstract <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer);

    public static void sendToServer(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundCustomPayloadPacket(packet.getIdentifier(), buffer));
        }
    }

    public static void sendToClient(ServerPlayer player, Packet<?> packet) {
        if (!Voicechat.SERVER.isCompatible(player)) {
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        player.connection.send(new ClientboundCustomPayloadPacket(packet.getIdentifier(), buffer));
    }

    public interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet);
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPacketListener handler, T packet);
    }

}

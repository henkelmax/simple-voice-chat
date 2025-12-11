package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.Packet;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VCByteBuf;
import io.netty.buffer.Unpooled;

public abstract class NetManager {

    public Channel<UpdateStatePacket> updateStateChannel;
    public Channel<PlayerStatePacket> playerStateChannel;
    public Channel<PlayerStatesPacket> playerStatesChannel;
    public Channel<RemovePlayerStatePacket> removePlayerStateChannel;
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
        removePlayerStateChannel = registerReceiver(RemovePlayerStatePacket.class, true, false);
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

    public static void sendToClient(de.maxhenkel.voicechat.api.ServerPlayer player, Packet<?> packet) {
        if (!Voicechat.SERVER.isCompatible(player)) {
            return;
        }
        VCByteBuf buffer = Voicechat.outSourcing.byteBufOf(Unpooled.buffer());
        packet.toBytes(buffer);
        Voicechat.outSourcing.sendCustomPacket(player, packet.getID(), buffer);
    }

    public interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayer player, Object handler, T packet);
    }

}

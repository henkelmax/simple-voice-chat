package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.EntityAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.LocationalAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.StaticAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.EntitySoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.LocationalSoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.StaticSoundPacketImpl;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class VoicechatServerApiImpl extends VoicechatApiImpl implements VoicechatServerApi {

    private final MinecraftServer server;

    public VoicechatServerApiImpl(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void sendEntitySoundPacketTo(VoicechatConnection connection, EntitySoundPacket p) {
        if (p instanceof EntitySoundPacketImpl packet) {
            sendPacket(connection, packet.getPacket());
        }
    }

    @Override
    public void sendLocationalSoundPacketTo(VoicechatConnection connection, LocationalSoundPacket p) {
        if (p instanceof LocationalSoundPacketImpl packet) {
            sendPacket(connection, packet.getPacket());
        }
    }

    @Override
    public void sendStaticSoundPacketTo(VoicechatConnection connection, StaticSoundPacket p) {
        if (p instanceof StaticSoundPacketImpl packet) {
            sendPacket(connection, packet.getPacket());
        }
    }

    @Nullable
    @Override
    public EntityAudioChannel createEntityAudioChannel(UUID channelId, Entity entity) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        return new EntityAudioChannelImpl(channelId, server, entity);
    }

    @Nullable
    @Override
    public LocationalAudioChannel createLocationalAudioChannel(UUID channelId, ServerLevel level, Vec3 initialPosition) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        return new LocationalAudioChannelImpl(channelId, server, level, initialPosition);
    }

    @Nullable
    @Override
    public StaticAudioChannel createStaticAudioChannel(UUID channelId, ServerLevel level, VoicechatConnection connection) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        if (connection instanceof VoicechatConnectionImpl conn) {
            return new StaticAudioChannelImpl(channelId, server, conn);
        }
        return null;
    }

    public static void sendPacket(VoicechatConnection receiver, SoundPacket<?> s) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        PlayerState state = server.getPlayerStateManager().getState(receiver.getPlayer().getUUID());
        if (state == null) {
            return;
        }
        if (PluginManager.instance().onSoundPacket(null, null, receiver.getPlayer(), state, s)) {
            return;
        }
        ClientConnection c = server.getConnections().get(receiver.getPlayer().getUUID());
        try {
            c.send(server, new NetworkMessage(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public VoicechatConnection getConnectionOf(UUID playerUuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        if (player == null) {
            return null;
        }
        return VoicechatConnectionImpl.fromPlayer(player);
    }

    @Override
    public Group createGroup(String name, @Nullable String password) {
        return new GroupImpl(new de.maxhenkel.voicechat.voice.server.Group(UUID.randomUUID(), name, password));
    }

    @Nullable
    @Override
    public UUID getSecret(UUID userId) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        return server.getSecret(userId);
    }

    @Override
    public Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Vec3 pos, double range, Predicate<ServerPlayer> filter) {
        return ServerWorldUtils.getPlayersInRange(level, pos, range, filter);
    }

}

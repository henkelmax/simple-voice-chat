package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.*;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.*;
import de.maxhenkel.voicechat.plugins.impl.packets.EntitySoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.LocationalSoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.StaticSoundPacketImpl;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoicechatServerApiImpl extends VoicechatApiImpl implements VoicechatServerApi {

    private static final VoicechatServerApiImpl INSTANCE = new VoicechatServerApiImpl();

    private VoicechatServerApiImpl() {

    }

    public static VoicechatServerApiImpl instance() {
        return INSTANCE;
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
    public LocationalAudioChannel createLocationalAudioChannel(UUID channelId, ServerLevel level, Position initialPosition) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        if (initialPosition instanceof PositionImpl p) {
            return new LocationalAudioChannelImpl(channelId, server, level, p);
        } else {
            throw new IllegalArgumentException("initialPosition is not an instance of PositionImpl");
        }
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

    @Override
    public AudioPlayer createAudioPlayer(AudioChannel audioChannel, OpusEncoder encoder, Supplier<short[]> audioSupplier) {
        return new AudioPlayerImpl(audioChannel, encoder, audioSupplier);
    }

    @Override
    public AudioPlayer createAudioPlayer(AudioChannel audioChannel, OpusEncoder encoder, short[] audio) {
        return new AudioPlayerImpl(audioChannel, encoder, new AudioSupplier(audio));
    }

    public static void sendPacket(VoicechatConnection receiver, SoundPacket<?> s) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        PlayerState state = server.getPlayerStateManager().getState(receiver.getPlayer().getUuid());
        if (state == null) {
            return;
        }
        if (PluginManager.instance().onSoundPacket(null, null, (org.bukkit.entity.Player) receiver.getPlayer().getPlayer(), state, s, SoundPacketEvent.SOURCE_PLUGIN)) {
            return;
        }
        ClientConnection c = server.getConnections().get(receiver.getPlayer().getUuid());
        try {
            c.send(server, new NetworkMessage(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public VoicechatConnection getConnectionOf(UUID playerUuid) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUuid);
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
    public Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Position pos, double range, Predicate<ServerPlayer> filter) {
        if (pos instanceof PositionImpl p) {
            return ServerWorldUtils.getPlayersInRange((World) level.getServerLevel(), p.getPosition(), range, player -> filter.test(new ServerPlayerImpl(player))).stream().map(ServerPlayerImpl::new).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Position is not an instance of PositionImpl");
        }
    }

    @Override
    public double getBroadcastRange() {
        return Math.max(Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.broadcastRange.get());
    }

    @Override
    public void registerVolumeCategory(VolumeCategory category) {
        if (!(category instanceof VolumeCategoryImpl c)) {
            throw new IllegalArgumentException("VolumeCategory is not an instance of VolumeCategoryImpl");
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getCategoryManager().addCategory(c);
    }

    @Override
    public void unregisterVolumeCategory(String categoryId) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getCategoryManager().removeCategory(categoryId);
    }

}

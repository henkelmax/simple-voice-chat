package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.*;
import de.maxhenkel.voicechat.api.audiolistener.AudioListener;
import de.maxhenkel.voicechat.api.audiolistener.PlayerAudioListener;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.*;
import de.maxhenkel.voicechat.plugins.impl.audiolistener.PlayerAudioListenerImpl;
import de.maxhenkel.voicechat.plugins.impl.audiosender.AudioSenderImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.EntitySoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.LocationalSoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.StaticSoundPacketImpl;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
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
        if (p instanceof EntitySoundPacketImpl) {
            EntitySoundPacketImpl packet = (EntitySoundPacketImpl) p;
            sendPacket(connection, packet.getPacket());
        }
    }

    @Override
    public void sendLocationalSoundPacketTo(VoicechatConnection connection, LocationalSoundPacket p) {
        if (p instanceof LocationalSoundPacketImpl) {
            LocationalSoundPacketImpl packet = (LocationalSoundPacketImpl) p;
            sendPacket(connection, packet.getPacket());
        }
    }

    @Override
    public void sendStaticSoundPacketTo(VoicechatConnection connection, StaticSoundPacket p) {
        if (p instanceof StaticSoundPacketImpl) {
            StaticSoundPacketImpl packet = (StaticSoundPacketImpl) p;
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
        if (initialPosition instanceof PositionImpl) {
            PositionImpl p = (PositionImpl) initialPosition;
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
        if (connection instanceof VoicechatConnectionImpl) {
            VoicechatConnectionImpl conn = (VoicechatConnectionImpl) connection;
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

    @Override
    public AudioSender createAudioSender(VoicechatConnection connection) {
        return new AudioSenderImpl(connection.getPlayer().getUuid());
    }

    @Override
    public boolean registerAudioSender(AudioSender sender) {
        if (!(sender instanceof AudioSenderImpl)) {
            return false;
        }
        return AudioSenderImpl.registerAudioSender((AudioSenderImpl) sender);
    }

    @Override
    public boolean unregisterAudioSender(AudioSender sender) {
        if (!(sender instanceof AudioSenderImpl)) {
            return false;
        }
        return AudioSenderImpl.unregisterAudioSender((AudioSenderImpl) sender);
    }

    @Override
    public PlayerAudioListener.Builder playerAudioListenerBuilder() {
        return new PlayerAudioListenerImpl.BuilderImpl();
    }

    @Override
    public boolean registerAudioListener(AudioListener listener) {
        return PluginManager.instance().registerAudioListener(listener);
    }

    @Override
    public boolean unregisterAudioListener(AudioListener listener) {
        return unregisterAudioListener(listener.getListenerId());
    }

    @Override
    public boolean unregisterAudioListener(UUID listenerId) {
        return PluginManager.instance().unregisterAudioListener(listenerId);
    }

    public static void sendPacket(VoicechatConnection receiver, SoundPacket<?> soundPacket) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }

        PlayerState state = server.getPlayerStateManager().getState(receiver.getPlayer().getUuid());
        if (state == null) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) receiver.getPlayer().getPlayer();

        @Nullable ClientConnection c = server.getConnections().get(receiver.getPlayer().getUuid());
        try {
            server.sendSoundPacket(null, null, player, state, c, soundPacket, SoundPacketEvent.SOURCE_PLUGIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public VoicechatConnection getConnectionOf(UUID playerUuid) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        EntityPlayerMP player = server.getServer().getPlayerList().getPlayerByUUID(playerUuid);
        if (player == null) {
            return null;
        }
        return VoicechatConnectionImpl.fromPlayer(player);
    }

    @Override
    public Group createGroup(String name, @Nullable String password) {
        return createGroup(name, password, false);
    }

    @Override
    public Group createGroup(String name, @Nullable String password, boolean persistent) {
        return groupBuilder().setName(name).setPassword(password).setPersistent(persistent).build();
    }

    @Override
    public Group.Builder groupBuilder() {
        return new GroupImpl.BuilderImpl();
    }

    @Override
    public boolean removeGroup(UUID groupId) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return false;
        }
        return server.getGroupManager().removeGroup(groupId);
    }

    @Nullable
    @Override
    public Group getGroup(UUID groupId) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        return new GroupImpl(server.getGroupManager().getGroup(groupId));
    }

    @Override
    public Collection<Group> getGroups() {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getGroupManager().getGroups().values().stream().map(group -> (Group) new GroupImpl(group)).collect(Collectors.toList());
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
        if (pos instanceof PositionImpl) {
            PositionImpl p = (PositionImpl) pos;
            return ServerWorldUtils.getPlayersInRange((WorldServer) level.getServerLevel(), p.getPosition(), range, player -> filter.test(new ServerPlayerImpl(player))).stream().map(ServerPlayerImpl::new).collect(Collectors.toList());
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
        if (!(category instanceof VolumeCategoryImpl)) {
            throw new IllegalArgumentException("VolumeCategory is not an instance of VolumeCategoryImpl");
        }
        VolumeCategoryImpl c = (VolumeCategoryImpl) category;
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getCategoryManager().addCategory(c);
        PluginManager.instance().onRegisterVolumeCategory(category);
    }

    @Override
    public void unregisterVolumeCategory(String categoryId) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        VolumeCategoryImpl category = server.getCategoryManager().removeCategory(categoryId);
        if (category != null) {
            PluginManager.instance().onUnregisterVolumeCategory(category);
        }
    }

    @Override
    public Collection<VolumeCategory> getVolumeCategories() {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getCategoryManager().getCategories().stream().map(VolumeCategory.class::cast).collect(Collectors.toList());
    }

}

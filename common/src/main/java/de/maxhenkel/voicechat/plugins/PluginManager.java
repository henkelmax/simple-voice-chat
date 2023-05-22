package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiolistener.AudioListener;
import de.maxhenkel.voicechat.api.audiolistener.PlayerAudioListener;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.*;
import de.maxhenkel.voicechat.plugins.impl.audiolistener.PlayerAudioListenerImpl;
import de.maxhenkel.voicechat.plugins.impl.events.*;
import de.maxhenkel.voicechat.plugins.impl.packets.*;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class PluginManager {

    private List<VoicechatPlugin> plugins;
    private Map<Class<? extends Event>, List<Consumer<? extends Event>>> events;
    private Map<UUID, List<PlayerAudioListener>> playerAudioListeners;

    public void init() {
        Voicechat.LOGGER.info("Loading plugins");
        plugins = CommonCompatibilityManager.INSTANCE.loadPlugins();
        Voicechat.LOGGER.info("Loaded {} plugin(s)", plugins.size());
        Voicechat.LOGGER.info("Initializing plugins");
        for (VoicechatPlugin plugin : plugins) {
            try {
                plugin.initialize(VoicechatServerApiImpl.instance());
            } catch (Throwable e) {
                Voicechat.LOGGER.warn("Failed to initialize plugin '{}'", plugin.getPluginId(), e);
            }
        }
        Voicechat.LOGGER.info("Initialized {} plugin(s)", plugins.size());
        gatherEvents();
        playerAudioListeners = new HashMap<>();
    }

    private void gatherEvents() {
        EventBuilder eventBuilder = EventBuilder.create();
        EventRegistration registration = eventBuilder::addEvent;
        for (VoicechatPlugin plugin : plugins) {
            Voicechat.LOGGER.info("Registering events for {}", plugin.getPluginId());
            try {
                plugin.registerEvents(registration);
            } catch (Throwable e) {
                Voicechat.LOGGER.warn("Failed to register events for plugin '{}'", plugin.getPluginId(), e);
            }
        }
        events = eventBuilder.build();
    }

    public boolean registerAudioListener(AudioListener l) {
        if (!(l instanceof PlayerAudioListener)) {
            return false;
        }
        PlayerAudioListener listener = (PlayerAudioListener) l;
        boolean exists = playerAudioListeners
                .values()
                .stream()
                .anyMatch(listeners ->
                        listeners
                                .stream()
                                .anyMatch(playerAudioListener -> playerAudioListener.getListenerId().equals(listener.getListenerId()))
                );

        if (exists) {
            return false;
        }

        playerAudioListeners.computeIfAbsent(listener.getPlayerUuid(), k -> new ArrayList<>()).add(listener);
        return true;
    }

    public boolean unregisterAudioListener(UUID listenerId) {
        boolean removed = playerAudioListeners
                .values()
                .stream()
                .anyMatch(listeners ->
                        listeners.removeIf(listener -> listener.getListenerId().equals(listenerId))
                );
        if (!removed) {
            return false;
        }
        playerAudioListeners.values().removeIf(List::isEmpty);
        return true;
    }

    public List<PlayerAudioListener> getPlayerAudioListeners(UUID playerUuid) {
        return playerAudioListeners.getOrDefault(playerUuid, Collections.emptyList());
    }

    public void onListenerAudio(UUID playerUuid, SoundPacket<?> packet) {
        if (playerUuid.equals(packet.getSender())) {
            return;
        }
        List<PlayerAudioListener> listeners = getPlayerAudioListeners(playerUuid);
        if (listeners.isEmpty()) {
            return;
        }

        SoundPacketImpl soundPacket;
        if (packet instanceof GroupSoundPacket) {
            soundPacket = new StaticSoundPacketImpl((GroupSoundPacket) packet);
        } else if (packet instanceof PlayerSoundPacket) {
            soundPacket = new EntitySoundPacketImpl((PlayerSoundPacket) packet);
        } else if (packet instanceof LocationSoundPacket) {
            soundPacket = new LocationalSoundPacketImpl((LocationSoundPacket) packet);
        } else {
            soundPacket = new SoundPacketImpl(packet);
        }

        for (PlayerAudioListener l : listeners) {
            if (l instanceof PlayerAudioListenerImpl) {
                ((PlayerAudioListenerImpl) l).getListener().accept(soundPacket);
            }
        }
    }

    public <T extends Event> boolean dispatchEvent(Class<? extends T> eventClass, T event) {
        List<Consumer<? extends Event>> events = this.events.get(eventClass);
        if (events == null) {
            return false;
        }
        for (Consumer<? extends Event> evt : events) {
            try {
                Consumer<T> e = (Consumer<T>) evt;
                e.accept(event);
                if (event.isCancelled()) {
                    break;
                }
            } catch (Throwable e) {
                Voicechat.LOGGER.warn("Failed to dispatch event '{}'", event.getClass().getSimpleName(), e);
            }
        }
        return event.isCancelled();
    }

    public VoicechatSocket getSocketImplementation(MinecraftServer server) {
        VoicechatServerStartingEventImpl event = new VoicechatServerStartingEventImpl();
        dispatchEvent(VoicechatServerStartingEvent.class, event);
        VoicechatSocket socket = event.getSocketImplementation();
        if (socket == null) {
            socket = new VoicechatSocketImpl();
            Voicechat.logDebug("Using default voicechat socket implementation");
        } else {
            Voicechat.LOGGER.info("Using custom voicechat socket implementation: {}", socket.getClass().getName());
        }
        return socket;
    }

    public ClientVoicechatSocket getClientSocketImplementation() {
        ClientVoicechatInitializationEventImpl event = new ClientVoicechatInitializationEventImpl();
        dispatchEvent(ClientVoicechatInitializationEvent.class, event);
        ClientVoicechatSocket socket = event.getSocketImplementation();
        if (socket == null) {
            socket = new ClientVoicechatSocketImpl();
            Voicechat.logDebug("Using default voicechat client socket implementation");
        } else {
            Voicechat.LOGGER.info("Using custom voicechat client socket implementation: {}", socket.getClass().getName());
        }
        return socket;
    }

    public String getVoiceHost(String voiceHost) {
        VoiceHostEventImpl event = new VoiceHostEventImpl(voiceHost);
        dispatchEvent(VoiceHostEvent.class, event);
        return event.getVoiceHost();
    }

    public void onRegisterVolumeCategory(VolumeCategory category) {
        dispatchEvent(RegisterVolumeCategoryEvent.class, new RegisterVolumeCategoryEventImpl(category));
    }

    public void onUnregisterVolumeCategory(VolumeCategory category) {
        dispatchEvent(UnregisterVolumeCategoryEvent.class, new UnregisterVolumeCategoryEventImpl(category));
    }

    public void onServerStarted() {
        dispatchEvent(VoicechatServerStartedEvent.class, new VoicechatServerStartedEventImpl());
    }

    public void onServerStopped() {
        dispatchEvent(VoicechatServerStoppedEvent.class, new VoicechatServerStoppedEventImpl());
    }

    public void onPlayerConnected(ServerPlayerEntity player) {
        dispatchEvent(PlayerConnectedEvent.class, new PlayerConnectedEventImpl(VoicechatConnectionImpl.fromPlayer(player)));
    }

    public void onPlayerDisconnected(UUID player) {
        dispatchEvent(PlayerDisconnectedEvent.class, new PlayerDisconnectedEventImpl(player));
    }

    public void onPlayerStateChanged(PlayerState state) {
        dispatchEvent(PlayerStateChangedEvent.class, new PlayerStateChangedEventImpl(state));
    }

    public boolean onJoinGroup(ServerPlayerEntity player, @Nullable Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(JoinGroupEvent.class, new JoinGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onCreateGroup(@Nullable ServerPlayerEntity player, @Nullable Group group) {
        if (group == null) {
            if (player == null) {
                return false;
            }
            return onLeaveGroup(player);
        }
        return dispatchEvent(CreateGroupEvent.class, new CreateGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onLeaveGroup(ServerPlayerEntity player) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return false;
        }
        @Nullable GroupImpl group = null;
        PlayerState state = server.getPlayerStateManager().getState(player.getUUID());
        if (state != null) {
            UUID groupUUID = state.getGroup();
            if (groupUUID != null) {
                Group g = server.getGroupManager().getGroup(groupUUID);
                if (g != null) {
                    group = new GroupImpl(g);
                }
            }
        }

        return dispatchEvent(LeaveGroupEvent.class, new LeaveGroupEventImpl(group, VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onRemoveGroup(Group group) {
        return dispatchEvent(RemoveGroupEvent.class, new RemoveGroupEventImpl(new GroupImpl(group)));
    }

    public boolean onMicPacket(ServerPlayerEntity player, PlayerState state, MicPacket packet) {
        return dispatchEvent(MicrophonePacketEvent.class, new MicrophonePacketEventImpl(
                new MicrophonePacketImpl(packet, player.getUUID()),
                new VoicechatConnectionImpl(player, state)
        ));
    }

    public boolean onSoundPacket(@Nullable ServerPlayerEntity sender, @Nullable PlayerState senderState, ServerPlayerEntity receiver, PlayerState receiverState, SoundPacket<?> p, String source) {
        VoicechatConnection senderConnection = null;
        if (sender != null && senderState != null) {
            senderConnection = new VoicechatConnectionImpl(sender, senderState);
        }

        VoicechatConnection receiverConnection = new VoicechatConnectionImpl(receiver, receiverState);
        if (p instanceof LocationSoundPacket) {
            LocationSoundPacket packet = (LocationSoundPacket) p;
            return dispatchEvent(LocationalSoundPacketEvent.class, new LocationalSoundPacketEventImpl(
                    new LocationalSoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof PlayerSoundPacket) {
            PlayerSoundPacket packet = (PlayerSoundPacket) p;
            return dispatchEvent(EntitySoundPacketEvent.class, new EntitySoundPacketEventImpl(
                    new EntitySoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof GroupSoundPacket) {
            GroupSoundPacket packet = (GroupSoundPacket) p;
            return dispatchEvent(StaticSoundPacketEvent.class, new StaticSoundPacketEventImpl(
                    new StaticSoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        }
        return false;
    }

    @Nullable
    public short[] onClientSound(short[] rawAudio, boolean whispering) {
        ClientSoundEventImpl clientSoundEvent = new ClientSoundEventImpl(rawAudio, whispering);
        boolean cancelled = dispatchEvent(ClientSoundEvent.class, clientSoundEvent);
        if (cancelled) {
            return null;
        }
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveEntityClientSound(UUID id, short[] rawAudio, boolean whispering, float distance) {
        ClientReceiveSoundEventImpl.EntitySoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.EntitySoundImpl(id, rawAudio, whispering, distance);
        dispatchEvent(ClientReceiveSoundEvent.EntitySound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveLocationalClientSound(UUID id, short[] rawAudio, Vector3d pos, float distance) {
        ClientReceiveSoundEventImpl.LocationalSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.LocationalSoundImpl(id, rawAudio, new PositionImpl(pos), distance);
        dispatchEvent(ClientReceiveSoundEvent.LocationalSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveStaticClientSound(UUID id, short[] rawAudio) {
        ClientReceiveSoundEventImpl.StaticSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.StaticSoundImpl(id, rawAudio);
        dispatchEvent(ClientReceiveSoundEvent.StaticSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public void onALSound(int source, @Nullable UUID channelId, @Nullable Vector3d pos, @Nullable String category, Class<? extends OpenALSoundEvent> eventClass) {
        dispatchEvent(eventClass, new OpenALSoundEventImpl(
                channelId,
                pos == null ? null : new PositionImpl(pos),
                category,
                source
        ));
    }

    public void onCreateALContext(long context, long device) {
        dispatchEvent(CreateOpenALContextEvent.class, new CreateOpenALContextEventImpl(
                context,
                device
        ));
    }

    public void onDestroyALContext(long context, long device) {
        dispatchEvent(DestroyOpenALContextEvent.class, new DestroyOpenALContextEventImpl(
                context,
                device
        ));
    }

    private static PluginManager instance;

    public static PluginManager instance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

}

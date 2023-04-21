package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.audiolistener.AudioListener;
import de.maxhenkel.voicechat.api.audiolistener.PlayerAudioListener;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatSocketImpl;
import de.maxhenkel.voicechat.plugins.impl.audiolistener.PlayerAudioListenerImpl;
import de.maxhenkel.voicechat.plugins.impl.events.*;
import de.maxhenkel.voicechat.plugins.impl.packets.*;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.Group;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class PluginManager {

    private List<VoicechatPlugin> plugins;
    private Map<Class<? extends Event>, List<Consumer<? extends Event>>> events;
    private Map<UUID, List<PlayerAudioListener>> playerAudioListeners;

    public void init() {
        Voicechat.LOGGER.info("Loading plugins");
        plugins = Voicechat.apiService.getPlugins();
        Voicechat.LOGGER.info("Loaded {} plugin(s)", plugins.size());
        Voicechat.LOGGER.info("Initializing plugins");
        for (VoicechatPlugin plugin : plugins) {
            try {
                plugin.initialize(VoicechatServerApiImpl.instance());
            } catch (Exception e) {
                e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
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
        SoundPacketImpl soundPacket = new SoundPacketImpl(packet);
        for (PlayerAudioListener l : listeners) {
            if (l instanceof PlayerAudioListenerImpl) {
                ((PlayerAudioListenerImpl) l).getListener().accept(soundPacket);
            }
        }
    }

    public <T extends Event> boolean dispatchEvent(Class<T> eventClass, T event) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return event.isCancelled();
    }

    public VoicechatSocket getSocketImplementation() {
        VoicechatServerStartingEventImpl event = new VoicechatServerStartingEventImpl();
        dispatchEvent(VoicechatServerStartingEvent.class, event);
        VoicechatSocket socket = event.getSocketImplementation();
        if (socket == null) {
            socket = new VoicechatSocketImpl();
            Voicechat.LOGGER.info("Using default voicechat socket implementation");
        } else {
            Voicechat.LOGGER.info("Using custom voicechat socket implementation: {}", socket.getClass().getName());
        }
        return socket;
    }

    public String getVoiceHost(String voiceHost) {
        VoiceHostEventImpl event = new VoiceHostEventImpl(voiceHost);
        dispatchEvent(VoiceHostEvent.class, event);
        return event.getVoiceHost();
    }

    public void onServerStarted() {
        dispatchEvent(VoicechatServerStartedEvent.class, new VoicechatServerStartedEventImpl());
    }

    public void onServerStopped() {
        dispatchEvent(VoicechatServerStoppedEvent.class, new VoicechatServerStoppedEventImpl());
    }

    public void onPlayerConnected(@Nullable Player player) {
        if (player == null) {
            return;
        }
        dispatchEvent(PlayerConnectedEvent.class, new PlayerConnectedEventImpl(VoicechatConnectionImpl.fromPlayer(player)));
    }

    public void onPlayerDisconnected(UUID player) {
        dispatchEvent(PlayerDisconnectedEvent.class, new PlayerDisconnectedEventImpl(player));
    }

    public void onPlayerStateChanged(PlayerState state) {
        dispatchEvent(PlayerStateChangedEvent.class, new PlayerStateChangedEventImpl(state));
    }

    public boolean onJoinGroup(Player player, @Nullable Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(JoinGroupEvent.class, new JoinGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onCreateGroup(@Nullable Player player, @Nullable Group group) {
        if (group == null) {
            if (player == null) {
                return false;
            }
            return onLeaveGroup(player);
        }
        return dispatchEvent(CreateGroupEvent.class, new CreateGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onLeaveGroup(Player player) {
        return dispatchEvent(LeaveGroupEvent.class, new LeaveGroupEventImpl(null, VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onRemoveGroup(Group group) {
        return dispatchEvent(RemoveGroupEvent.class, new RemoveGroupEventImpl(new GroupImpl(group)));
    }

    public boolean onMicPacket(Player player, PlayerState state, MicPacket packet) {
        return dispatchEvent(MicrophonePacketEvent.class, new MicrophonePacketEventImpl(
                new MicrophonePacketImpl(packet, player.getUniqueId()),
                new VoicechatConnectionImpl(player, state)
        ));
    }

    public boolean onSoundPacket(@Nullable Player sender, @Nullable PlayerState senderState, Player receiver, PlayerState receiverState, SoundPacket<?> p, String source) {
        VoicechatConnection senderConnection = null;
        if (sender != null && senderState != null) {
            senderConnection = new VoicechatConnectionImpl(sender, senderState);
        }

        VoicechatConnection receiverConnection = new VoicechatConnectionImpl(receiver, receiverState);
        if (p instanceof LocationSoundPacket) {
            return dispatchEvent(LocationalSoundPacketEvent.class, new LocationalSoundPacketEventImpl(
                    new LocationalSoundPacketImpl((LocationSoundPacket) p),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof PlayerSoundPacket) {
            return dispatchEvent(EntitySoundPacketEvent.class, new EntitySoundPacketEventImpl(
                    new EntitySoundPacketImpl((PlayerSoundPacket) p),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof GroupSoundPacket) {
            return dispatchEvent(StaticSoundPacketEvent.class, new StaticSoundPacketEventImpl(
                    new StaticSoundPacketImpl((GroupSoundPacket) p),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        }
        return false;
    }

    private static PluginManager instance;

    public static PluginManager instance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

}

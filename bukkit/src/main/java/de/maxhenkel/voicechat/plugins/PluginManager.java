package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatSocketImpl;
import de.maxhenkel.voicechat.plugins.impl.events.*;
import de.maxhenkel.voicechat.plugins.impl.packets.EntitySoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.LocationalSoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.MicrophonePacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.StaticSoundPacketImpl;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.Group;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PluginManager {

    private List<VoicechatPlugin> plugins;
    private Map<Class<? extends Event>, List<Consumer<? extends Event>>> events;

    public void init(Plugin p) {
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

    public boolean onJoinGroup(Player player, @Nullable Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(JoinGroupEvent.class, new JoinGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onCreateGroup(Player player, Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(CreateGroupEvent.class, new CreateGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onLeaveGroup(Player player) {
        return dispatchEvent(LeaveGroupEvent.class, new LeaveGroupEventImpl(null, VoicechatConnectionImpl.fromPlayer(player)));
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
        if (p instanceof LocationSoundPacket packet) {
            return dispatchEvent(LocationalSoundPacketEvent.class, new LocationalSoundPacketEventImpl(
                    new LocationalSoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof PlayerSoundPacket packet) {
            return dispatchEvent(EntitySoundPacketEvent.class, new EntitySoundPacketEventImpl(
                    new EntitySoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof GroupSoundPacket packet) {
            return dispatchEvent(StaticSoundPacketEvent.class, new StaticSoundPacketEventImpl(
                    new StaticSoundPacketImpl(packet),
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

package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.*;
import de.maxhenkel.voicechat.plugins.impl.events.*;
import de.maxhenkel.voicechat.plugins.impl.packets.EntitySoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.LocationalSoundPacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.MicrophonePacketImpl;
import de.maxhenkel.voicechat.plugins.impl.packets.StaticSoundPacketImpl;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.Group;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PluginManager {

    private List<VoicechatPlugin> plugins;
    private Map<Class<? extends Event>, List<Consumer<? extends Event>>> events;

    public void init() {
        Voicechat.LOGGER.info("Loading plugins");
        plugins = CommonCompatibilityManager.INSTANCE.loadPlugins();
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
            } catch (Exception e) {
                e.printStackTrace();
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

    public void onServerStarted() {
        dispatchEvent(VoicechatServerStartedEvent.class, new VoicechatServerStartedEventImpl());
    }

    public void onServerStopped() {
        dispatchEvent(VoicechatServerStoppedEvent.class, new VoicechatServerStoppedEventImpl());
    }

    public void onPlayerConnected(ServerPlayer player) {
        dispatchEvent(PlayerConnectedEvent.class, new PlayerConnectedEventImpl(VoicechatConnectionImpl.fromPlayer(player)));
    }

    public void onPlayerDisconnected(UUID player) {
        dispatchEvent(PlayerDisconnectedEvent.class, new PlayerDisconnectedEventImpl(player));
    }

    public boolean onJoinGroup(ServerPlayer player, @Nullable Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(JoinGroupEvent.class, new JoinGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onCreateGroup(@Nullable ServerPlayer player, @Nullable Group group) {
        if (group == null) {
            if (player == null) {
                return false;
            }
            return onLeaveGroup(player);
        }
        return dispatchEvent(CreateGroupEvent.class, new CreateGroupEventImpl(new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onLeaveGroup(ServerPlayer player) {
        return dispatchEvent(LeaveGroupEvent.class, new LeaveGroupEventImpl(null, VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onRemoveGroup(Group group) {
        return dispatchEvent(RemoveGroupEvent.class, new RemoveGroupEventImpl(new GroupImpl(group)));
    }

    public boolean onMicPacket(ServerPlayer player, PlayerState state, MicPacket packet) {
        return dispatchEvent(MicrophonePacketEvent.class, new MicrophonePacketEventImpl(
                new MicrophonePacketImpl(packet, player.getUUID()),
                new VoicechatConnectionImpl(player, state)
        ));
    }

    public boolean onSoundPacket(@Nullable ServerPlayer sender, @Nullable PlayerState senderState, ServerPlayer receiver, PlayerState receiverState, SoundPacket<?> p, String source) {
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

    public short[] onReceiveLocationalClientSound(UUID id, short[] rawAudio, Vec3 pos, float distance) {
        ClientReceiveSoundEventImpl.LocationalSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.LocationalSoundImpl(id, rawAudio, new PositionImpl(pos), distance);
        dispatchEvent(ClientReceiveSoundEvent.LocationalSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveStaticClientSound(UUID id, short[] rawAudio) {
        ClientReceiveSoundEventImpl.StaticSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.StaticSoundImpl(id, rawAudio);
        dispatchEvent(ClientReceiveSoundEvent.StaticSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public void onALSound(int source, @Nullable UUID channelId, @Nullable Vec3 pos, @Nullable String category, Class<? extends OpenALSoundEvent> eventClass) {
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

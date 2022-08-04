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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;

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
                plugin.initialize(new VoicechatApiImpl());
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
        VoicechatServerStartingEventImpl event = new VoicechatServerStartingEventImpl(new VoicechatServerApiImpl(server));
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
        ClientVoicechatInitializationEventImpl event = new ClientVoicechatInitializationEventImpl(new VoicechatClientApiImpl());
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

    public String getVoiceHost(ServerPlayerEntity player, String voiceHost) {
        VoiceHostEventImpl event = new VoiceHostEventImpl(new VoicechatServerApiImpl(player.server), voiceHost);
        dispatchEvent(VoiceHostEvent.class, event);
        return event.getVoiceHost();
    }

    public void onServerStarted(MinecraftServer server) {
        dispatchEvent(VoicechatServerStartedEvent.class, new VoicechatServerStartedEventImpl(new VoicechatServerApiImpl(server)));
    }

    public void onServerStopped(MinecraftServer server) {
        dispatchEvent(VoicechatServerStoppedEvent.class, new VoicechatServerStoppedEventImpl(new VoicechatServerApiImpl(server)));
    }

    public void onPlayerConnected(ServerPlayerEntity player) {
        dispatchEvent(PlayerConnectedEvent.class, new PlayerConnectedEventImpl(new VoicechatServerApiImpl(player.server), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public void onPlayerDisconnected(MinecraftServer server, UUID player) {
        dispatchEvent(PlayerDisconnectedEvent.class, new PlayerDisconnectedEventImpl(new VoicechatServerApiImpl(server), player));
    }

    public boolean onJoinGroup(ServerPlayerEntity player, @Nullable Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(JoinGroupEvent.class, new JoinGroupEventImpl(new VoicechatServerApiImpl(player.server), new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onCreateGroup(ServerPlayerEntity player, Group group) {
        if (group == null) {
            return onLeaveGroup(player);
        }
        return dispatchEvent(CreateGroupEvent.class, new CreateGroupEventImpl(new VoicechatServerApiImpl(player.server), new GroupImpl(group), VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onLeaveGroup(ServerPlayerEntity player) {
        return dispatchEvent(LeaveGroupEvent.class, new LeaveGroupEventImpl(new VoicechatServerApiImpl(player.server), null, VoicechatConnectionImpl.fromPlayer(player)));
    }

    public boolean onMicPacket(ServerPlayerEntity player, PlayerState state, MicPacket packet) {
        return dispatchEvent(MicrophonePacketEvent.class, new MicrophonePacketEventImpl(
                new VoicechatServerApiImpl(player.server),
                new MicrophonePacketImpl(packet, player.getUUID()),
                new VoicechatConnectionImpl(player, state)
        ));
    }

    public boolean onSoundPacket(@Nullable ServerPlayerEntity sender, @Nullable PlayerState senderState, ServerPlayerEntity receiver, PlayerState receiverState, SoundPacket<?> p, String source) {
        VoicechatServerApi api = new VoicechatServerApiImpl(receiver.server);
        VoicechatConnection senderConnection = null;
        if (sender != null && senderState != null) {
            senderConnection = new VoicechatConnectionImpl(sender, senderState);
        }

        VoicechatConnection receiverConnection = new VoicechatConnectionImpl(receiver, receiverState);
        if (p instanceof LocationSoundPacket) {
            LocationSoundPacket packet = (LocationSoundPacket) p;
            return dispatchEvent(LocationalSoundPacketEvent.class, new LocationalSoundPacketEventImpl(
                    api,
                    new LocationalSoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof PlayerSoundPacket) {
            PlayerSoundPacket packet = (PlayerSoundPacket) p;
            return dispatchEvent(EntitySoundPacketEvent.class, new EntitySoundPacketEventImpl(
                    api,
                    new EntitySoundPacketImpl(packet),
                    senderConnection,
                    receiverConnection,
                    source
            ));
        } else if (p instanceof GroupSoundPacket) {
            GroupSoundPacket packet = (GroupSoundPacket) p;
            return dispatchEvent(StaticSoundPacketEvent.class, new StaticSoundPacketEventImpl(
                    api,
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
        ClientSoundEventImpl clientSoundEvent = new ClientSoundEventImpl(new VoicechatClientApiImpl(), rawAudio, whispering);
        boolean cancelled = dispatchEvent(ClientSoundEvent.class, clientSoundEvent);
        if (cancelled) {
            return null;
        }
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveEntityClientSound(UUID id, short[] rawAudio, boolean whispering, float distance) {
        ClientReceiveSoundEventImpl.EntitySoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.EntitySoundImpl(new VoicechatClientApiImpl(), id, rawAudio, whispering, distance);
        dispatchEvent(ClientReceiveSoundEvent.EntitySound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveLocationalClientSound(UUID id, short[] rawAudio, Vector3d pos, float distance) {
        ClientReceiveSoundEventImpl.LocationalSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.LocationalSoundImpl(new VoicechatClientApiImpl(), id, rawAudio, new PositionImpl(pos), distance);
        dispatchEvent(ClientReceiveSoundEvent.LocationalSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveStaticClientSound(UUID id, short[] rawAudio) {
        ClientReceiveSoundEventImpl.StaticSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.StaticSoundImpl(new VoicechatClientApiImpl(), id, rawAudio);
        dispatchEvent(ClientReceiveSoundEvent.StaticSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public void onALSound(int source, @Nullable UUID channelId, @Nullable Vector3d pos, @Nullable String category, Class<? extends OpenALSoundEvent> eventClass) {
        dispatchEvent(eventClass, new OpenALSoundEventImpl(
                new VoicechatClientApiImpl(),
                channelId,
                pos == null ? null : new PositionImpl(pos),
                category,
                source
        ));
    }

    public void onCreateALContext(long context, long device) {
        dispatchEvent(CreateOpenALContextEvent.class, new CreateOpenALContextEventImpl(
                new VoicechatClientApiImpl(),
                context,
                device
        ));
    }

    public void onDestroyALContext(long context, long device) {
        dispatchEvent(DestroyOpenALContextEvent.class, new DestroyOpenALContextEventImpl(
                new VoicechatClientApiImpl(),
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

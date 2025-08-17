package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.ClientVoicechatSocketImpl;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.plugins.impl.events.*;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ClientPluginManager {

    private final PluginManager pluginManager;

    public ClientPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * We are caching the event to avoid creating a new one every frame
     */
    private final NameTagIconRenderEventImpl cachedRenderEvent = new NameTagIconRenderEventImpl();

    public boolean shouldRenderPlayerIcons(UUID entityId) {
        cachedRenderEvent.setEntityId(entityId);
        cachedRenderEvent.setCancelled(false);
        return !pluginManager.dispatchEvent(NameTagIconRenderEvent.class, cachedRenderEvent);
    }

    public ClientVoicechatSocket getClientSocketImplementation() {
        ClientVoicechatInitializationEventImpl event = new ClientVoicechatInitializationEventImpl();
        pluginManager.dispatchEvent(ClientVoicechatInitializationEvent.class, event);
        ClientVoicechatSocket socket = event.getSocketImplementation();
        if (socket == null) {
            socket = new ClientVoicechatSocketImpl();
            Voicechat.LOGGER.debug("Using default voicechat client socket implementation");
        } else {
            Voicechat.LOGGER.info("Using custom voicechat client socket implementation: {}", socket.getClass().getName());
        }
        return socket;
    }

    @Nullable
    public short[] onMergeClientSound(@Nullable short[] rawAudio) {
        MergeClientSoundEventImpl event = new MergeClientSoundEventImpl();
        pluginManager.dispatchEvent(MergeClientSoundEvent.class, event);
        List<short[]> audioToMerge = event.getAudioToMerge();
        if (audioToMerge == null) {
            return rawAudio;
        }
        if (rawAudio != null) {
            audioToMerge.add(0, rawAudio);
        }
        return AudioUtils.combineAudio(audioToMerge);
    }

    @Nullable
    public short[] onClientSound(short[] rawAudio, boolean whispering) {
        ClientSoundEventImpl clientSoundEvent = new ClientSoundEventImpl(rawAudio, whispering);
        boolean cancelled = pluginManager.dispatchEvent(ClientSoundEvent.class, clientSoundEvent);
        if (cancelled) {
            return null;
        }
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveEntityClientSound(UUID id, UUID entity, short[] rawAudio, boolean whispering, float distance) {
        ClientReceiveSoundEventImpl.EntitySoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.EntitySoundImpl(id, entity, rawAudio, whispering, distance);
        pluginManager.dispatchEvent(ClientReceiveSoundEvent.EntitySound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveLocationalClientSound(UUID id, short[] rawAudio, Vec3 pos, float distance) {
        ClientReceiveSoundEventImpl.LocationalSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.LocationalSoundImpl(id, rawAudio, new PositionImpl(pos), distance);
        pluginManager.dispatchEvent(ClientReceiveSoundEvent.LocationalSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public short[] onReceiveStaticClientSound(UUID id, short[] rawAudio) {
        ClientReceiveSoundEventImpl.StaticSoundImpl clientSoundEvent = new ClientReceiveSoundEventImpl.StaticSoundImpl(id, rawAudio);
        pluginManager.dispatchEvent(ClientReceiveSoundEvent.StaticSound.class, clientSoundEvent);
        return clientSoundEvent.getRawAudio();
    }

    public void onALSound(int source, @Nullable UUID channelId, @Nullable Vec3 pos, @Nullable String category, Class<? extends OpenALSoundEvent> eventClass) {
        pluginManager.dispatchEvent(eventClass, new OpenALSoundEventImpl(
                channelId,
                pos == null ? null : new PositionImpl(pos),
                category,
                source
        ));
    }

    public void onCreateALContext(long context, long device) {
        pluginManager.dispatchEvent(CreateOpenALContextEvent.class, new CreateOpenALContextEventImpl(
                context,
                device
        ));
    }

    public void onDestroyALContext(long context, long device) {
        pluginManager.dispatchEvent(DestroyOpenALContextEvent.class, new DestroyOpenALContextEventImpl(
                context,
                device
        ));
    }

    private static ClientPluginManager instance;

    public static ClientPluginManager instance() {
        if (instance == null) {
            instance = new ClientPluginManager(PluginManager.instance());
        }
        return instance;
    }

}

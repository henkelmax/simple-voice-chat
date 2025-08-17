package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.api.config.ConfigAccessor;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientEntityAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientLocationalAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientStaticAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.config.ConfigAccessorImpl;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoicechatClientApiImpl extends VoicechatApiImpl implements VoicechatClientApi {

    @Deprecated
    public static final VoicechatClientApiImpl INSTANCE = new VoicechatClientApiImpl();

    private VoicechatClientApiImpl() {

    }

    public static VoicechatClientApi instance() {
        return ClientCompatibilityManager.INSTANCE.getClientApi();
    }

    @Override
    public boolean isMuted() {
        return ClientManager.getPlayerStateManager().isMuted();
    }

    @Override
    public boolean isDisabled(@Nullable UUID playerId) {
        if (playerId == null) {
            return ClientManager.getPlayerStateManager().isDisabled();
        }
        return ClientManager.getPlayerStateManager().isPlayerDisabled(playerId);
    }

    @Override
    public boolean isDisconnected(@Nullable UUID playerId) {
        if (playerId == null) {
            return ClientManager.getPlayerStateManager().isDisconnected();
        }
        return ClientManager.getPlayerStateManager().isPlayerDisconnected(playerId);
    }

    @Override
    public boolean isTalking(@Nullable UUID playerId) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return false;
        }
        if (playerId == null) {
            MicThread micThread = client.getMicThread();
            if (micThread == null) {
                return false;
            }
            return micThread.isTalking();
        }
        client.getTalkCache().isTalking(playerId);
        return false;
    }

    @Override
    public boolean isWhispering(@Nullable UUID playerId) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return false;
        }
        if (playerId == null) {
            MicThread micThread = client.getMicThread();
            if (micThread == null) {
                return false;
            }
            return micThread.isWhispering();
        }
        client.getTalkCache().isWhispering(playerId);
        return false;
    }

    @Override
    public boolean isPushToTalkKeyPressed() {
        return ClientManager.getPttKeyHandler().isPTTDown();
    }

    @Override
    public boolean isWhisperKeyPressed() {
        return ClientManager.getPttKeyHandler().isWhisperDown();
    }

    @Override
    @Nullable
    public Group getGroup() {
        ClientPlayerStateManager playerStateManager = ClientManager.getPlayerStateManager();
        if (playerStateManager.getGroupID() == null) {
            return null;
        }
        ClientGroup group = playerStateManager.getGroup();
        if (group == null) {
            return null;
        }
        return new ClientGroupImpl(group);
    }

    @Override
    public ClientEntityAudioChannel createEntityAudioChannel(UUID uuid) {
        return new ClientEntityAudioChannelImpl(uuid, uuid);
    }

    @Override
    public ClientEntityAudioChannel createEntityAudioChannel(UUID uuid, Entity entity) {
        return new ClientEntityAudioChannelImpl(uuid, entity.getUuid());
    }

    @Override
    public ClientLocationalAudioChannel createLocationalAudioChannel(UUID uuid, Position position) {
        return new ClientLocationalAudioChannelImpl(uuid, position);
    }

    @Override
    public ClientStaticAudioChannel createStaticAudioChannel(UUID uuid) {
        return new ClientStaticAudioChannelImpl(uuid);
    }

    @Override
    public void unregisterClientVolumeCategory(String categoryId) {
        ClientManager.getCategoryManager().removeCategory(categoryId);
    }

    @Override
    public ConfigAccessor getClientConfig() {
        return new ConfigAccessorImpl(VoicechatClient.CLIENT_CONFIG.disabled.getConfig());
    }

    @Override
    public void registerClientVolumeCategory(VolumeCategory category) {
        if (!(category instanceof VolumeCategoryImpl c)) {
            throw new IllegalArgumentException("VolumeCategory is not an instance of VolumeCategoryImpl");
        }
        ClientManager.getCategoryManager().addCategory(c);
    }

    @Override
    public double getVoiceChatDistance() {
        return ClientUtils.getDefaultDistanceClient();
    }
}

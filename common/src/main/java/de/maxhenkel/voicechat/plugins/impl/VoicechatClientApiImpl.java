package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientEntityAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientLocationalAudioChannelImpl;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.ClientStaticAudioChannelImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoicechatClientApiImpl extends VoicechatApiImpl implements VoicechatClientApi {

    @Override
    public boolean isMuted() {
        return ClientManager.getPlayerStateManager().isMuted();
    }

    @Override
    public boolean isDisabled() {
        return ClientManager.getPlayerStateManager().isDisabled();
    }

    @Override
    public boolean isDisconnected() {
        return ClientManager.getPlayerStateManager().isDisconnected();
    }

    @Override
    @Nullable
    public Group getGroup() {
        ClientGroup group = ClientManager.getPlayerStateManager().getGroup();
        if (group == null) {
            return null;
        }
        return new ClientGroupImpl(group);
    }

    @Override
    public ClientEntityAudioChannel createEntityAudioChannel(UUID uuid) {
        return new ClientEntityAudioChannelImpl(uuid);
    }

    @Override
    public ClientLocationalAudioChannel createLocationalAudioChannel(UUID uuid, Position position) {
        return new ClientLocationalAudioChannelImpl(uuid, position);
    }

    @Override
    public ClientStaticAudioChannel createStaticAudioChannel(UUID uuid) {
        return new ClientStaticAudioChannelImpl(uuid);
    }
}

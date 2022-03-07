package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;

import javax.annotation.Nullable;
import java.util.UUID;

public class OpenALSoundEventImpl extends ClientEventImpl implements OpenALSoundEvent, OpenALSoundEvent.Pre, OpenALSoundEvent.Post {

    @Nullable
    protected PositionImpl position;
    @Nullable
    protected UUID channelId;
    protected int source;

    public OpenALSoundEventImpl(VoicechatClientApi api, @Nullable UUID channelId, @Nullable PositionImpl position, int source) {
        super(api);
        this.channelId = channelId;
        this.position = position;
        this.source = source;
    }

    @Override
    @Nullable
    public Position getPosition() {
        return position;
    }

    @Override
    @Nullable
    public UUID getChannelId() {
        return channelId;
    }

    @Override
    public int getSource() {
        return source;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}

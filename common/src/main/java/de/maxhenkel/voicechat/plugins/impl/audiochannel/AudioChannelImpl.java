package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public abstract class AudioChannelImpl implements AudioChannel {

    protected UUID channelId;
    protected Server server;
    protected AtomicLong sequenceNumber;
    @Nullable
    protected Predicate<ServerPlayer> filter;

    public AudioChannelImpl(UUID channelId, Server server) {
        this.channelId = channelId;
        this.server = server;
        this.sequenceNumber = new AtomicLong();
    }

    @Override
    public void setFilter(Predicate<ServerPlayer> filter) {
        this.filter = filter;
    }

    @Override
    public UUID getId() {
        return channelId;
    }

    @Override
    public boolean isClosed() {
        return server.isClosed();
    }
}

package de.maxhenkel.voicechat.events;

import io.netty.channel.Channel;

public interface IClientConnection {
    Channel getChannel();
}

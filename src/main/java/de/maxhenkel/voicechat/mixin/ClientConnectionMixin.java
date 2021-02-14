package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.events.IClientConnection;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements IClientConnection {

    @Shadow
    private Channel channel;


    @Override
    public Channel getChannel() {
        return channel;
    }
}

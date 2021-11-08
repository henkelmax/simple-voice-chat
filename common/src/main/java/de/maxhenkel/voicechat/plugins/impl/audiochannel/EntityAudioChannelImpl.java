package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class EntityAudioChannelImpl extends AudioChannelImpl implements EntityAudioChannel {

    protected Entity entity;
    protected boolean whispering;

    public EntityAudioChannelImpl(UUID channelId, Server server, Entity entity) {
        super(channelId, server);
        this.entity = entity;
        this.whispering = false;
    }

    @Override
    public void setWhispering(boolean whispering) {
        this.whispering = whispering;
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public void updateEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new PlayerSoundPacket(channelId, opusData, sequenceNumber.getAndIncrement(), whispering));
    }

    @Override
    public void send(MicrophonePacket microphonePacket) {
        broadcast(new PlayerSoundPacket(channelId, microphonePacket.getOpusEncodedData(), sequenceNumber.getAndIncrement(), whispering));
    }

    @Override
    public void flush() {
        broadcast(new PlayerSoundPacket(channelId, new byte[0], sequenceNumber.getAndIncrement(), whispering));
    }

    private void broadcast(PlayerSoundPacket packet) {
        if (entity.getEntity() instanceof net.minecraft.world.entity.Entity entity) {
            server.broadcast(ServerWorldUtils.getPlayersInRange((ServerLevel) entity.level, entity.getEyePosition(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), filter == null ? player -> true : player -> filter.test(new ServerPlayerImpl(player))), packet, null, null, null);
        }
    }

}

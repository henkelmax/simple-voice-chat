package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class EntityAudioChannelImpl extends AudioChannelImpl implements EntityAudioChannel {

    protected Entity entity;
    protected boolean whispering;
    protected float distance;

    public EntityAudioChannelImpl(UUID channelId, Server server, Entity entity) {
        super(channelId, server);
        this.entity = entity;
        this.whispering = false;
        this.distance = Utils.getDefaultDistance();
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
    public float getDistance() {
        return distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new PlayerSoundPacket(entity.getUuid(), opusData, sequenceNumber.getAndIncrement(), whispering, distance, category));
    }

    @Override
    public void send(MicrophonePacket microphonePacket) {
        broadcast(new PlayerSoundPacket(entity.getUuid(), microphonePacket.getOpusEncodedData(), sequenceNumber.getAndIncrement(), whispering, distance, category));
    }

    @Override
    public void flush() {
        broadcast(new PlayerSoundPacket(entity.getUuid(), new byte[0], sequenceNumber.getAndIncrement(), whispering, distance, category));
    }

    private void broadcast(PlayerSoundPacket packet) {
        if (entity.getEntity() instanceof net.minecraft.world.entity.Entity entity) {
            server.broadcast(ServerWorldUtils.getPlayersInRange((ServerLevel) entity.level, entity.getEyePosition(), server.getBroadcastRange(distance), filter == null ? player -> true : player -> filter.test(new ServerPlayerImpl(player))), packet, null, null, null, SoundPacketEvent.SOURCE_PLUGIN);
        }
    }

}

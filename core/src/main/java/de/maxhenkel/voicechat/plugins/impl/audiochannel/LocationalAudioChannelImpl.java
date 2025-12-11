package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;

import java.util.UUID;

public class LocationalAudioChannelImpl extends AudioChannelImpl implements LocationalAudioChannel {

    protected ServerLevel level;
    protected Position position;
    protected float distance;

    public LocationalAudioChannelImpl(UUID channelId, Server server, ServerLevel level, Position position) {
        super(channelId, server);
        this.level = level;
        this.position = position;
        this.distance = Utils.getDefaultDistanceServer();
    }

    @Override
    public void updateLocation(Position position) {
        this.position = position;
    }

    @Override
    public Position getLocation() {
        return position;
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
        broadcast(new LocationSoundPacket(channelId, channelId, position, opusData, sequenceNumber.getAndIncrement(), distance, category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        broadcast(new LocationSoundPacket(channelId, channelId, position, new byte[0], sequenceNumber.getAndIncrement(), distance, category));
    }

    private void broadcast(LocationSoundPacket packet) {
        server.broadcast(ServerWorldUtils.getPlayersInRange(level, position, server.getBroadcastRange(distance), filter == null ? player -> true : player -> filter.test(player)), packet, null, null, null, SoundPacketEvent.SOURCE_PLUGIN);
    }

}

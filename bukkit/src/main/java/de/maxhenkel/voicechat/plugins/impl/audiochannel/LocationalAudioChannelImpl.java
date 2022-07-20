package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import org.bukkit.World;

import java.util.UUID;

public class LocationalAudioChannelImpl extends AudioChannelImpl implements LocationalAudioChannel {

    protected ServerLevel level;
    protected PositionImpl position;
    protected float distance;

    public LocationalAudioChannelImpl(UUID channelId, Server server, ServerLevel level, PositionImpl position) {
        super(channelId, server);
        this.level = level;
        this.position = position;
        this.distance = Utils.getDefaultDistance();
    }

    @Override
    public void updateLocation(Position position) {
        if (position instanceof PositionImpl p) {
            this.position = p;
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
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
        broadcast(new LocationSoundPacket(channelId, position.getPosition(), opusData, sequenceNumber.getAndIncrement(), distance, category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        broadcast(new LocationSoundPacket(channelId, position.getPosition(), new byte[0], sequenceNumber.getAndIncrement(), distance, category));
    }

    private void broadcast(LocationSoundPacket packet) {
        server.broadcast(ServerWorldUtils.getPlayersInRange((World) level.getServerLevel(), position.getPosition(), server.getBroadcastRange(distance), filter == null ? player -> true : player -> filter.test(new ServerPlayerImpl(player))), packet, null, null, null, SoundPacketEvent.SOURCE_PLUGIN);
    }

}

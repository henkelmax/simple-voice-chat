package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LocationalAudioChannelImpl extends AudioChannelImpl implements LocationalAudioChannel {

    protected ServerLevel level;
    protected Vec3 position;

    public LocationalAudioChannelImpl(UUID channelId, Server server, ServerLevel level, Vec3 position) {
        super(channelId, server);
        this.level = level;
        this.position = position;
    }

    @Override
    public void updateLocation(Vec3 position) {
        this.position = position;
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new LocationSoundPacket(channelId, position, opusData, sequenceNumber.getAndIncrement()));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        broadcast(new LocationSoundPacket(channelId, position, new byte[0], sequenceNumber.getAndIncrement()));
    }

    private void broadcast(LocationSoundPacket packet) {
        server.broadcast(ServerWorldUtils.getPlayersInRange(level, position, Voicechat.SERVER_CONFIG.voiceChatDistance.get(), filter == null ? player -> true : filter), packet, null, null, null);
    }

}

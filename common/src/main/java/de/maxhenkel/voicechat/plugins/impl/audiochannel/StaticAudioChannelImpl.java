package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaticAudioChannelImpl extends AudioChannelImpl implements StaticAudioChannel {

    protected final List<VoicechatConnectionImpl> connections;

    public StaticAudioChannelImpl(UUID channelId, Server server) {
        super(channelId, server);
        this.connections = new ArrayList<>();
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new GroupSoundPacket(channelId, channelId, opusData, sequenceNumber.getAndIncrement(), category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        GroupSoundPacket packet = new GroupSoundPacket(channelId, channelId, new byte[0], sequenceNumber.getAndIncrement(), category);
        broadcast(packet);
    }

    private void broadcast(GroupSoundPacket packet) {
        synchronized (connections) {
            for (VoicechatConnectionImpl connection : connections) {
                VoicechatServerApiImpl.sendPacket(connection, packet);
            }
        }
    }

    @Override
    public void addTarget(VoicechatConnection target) {
        synchronized (connections) {
            connections.removeIf(connection -> connection.getPlayer().getUuid().equals(target.getPlayer().getUuid()));
            if (target instanceof VoicechatConnectionImpl) {
                connections.add((VoicechatConnectionImpl) target);
            }
        }
    }

    @Override
    public void removeTarget(VoicechatConnection target) {
        synchronized (connections) {
            connections.removeIf(connection -> connection.getPlayer().getUuid().equals(target.getPlayer().getUuid()));
        }
    }

    @Override
    public void clearTargets() {
        synchronized (connections) {
            connections.clear();
        }
    }
}

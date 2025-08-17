package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.VoiceDistanceEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

import javax.annotation.Nullable;

public class VoiceDistanceEventImpl extends ServerEventImpl implements VoiceDistanceEvent {

    private final MicrophonePacket packet;
    private final VoicechatConnection senderConnection;
    private float distance;

    public VoiceDistanceEventImpl(MicrophonePacket packet, VoicechatConnection senderConnection, float distance) {
        this.packet = packet;
        this.senderConnection = senderConnection;
        this.distance = distance;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public MicrophonePacket getPacket() {
        return packet;
    }

    @Nullable
    @Override
    public VoicechatConnection getSenderConnection() {
        return senderConnection;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }
}

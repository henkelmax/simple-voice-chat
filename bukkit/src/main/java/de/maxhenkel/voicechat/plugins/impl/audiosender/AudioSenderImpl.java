package de.maxhenkel.voicechat.plugins.impl.audiosender;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AudioSenderImpl implements AudioSender {

    private static final Map<UUID, AudioSenderImpl> AUDIO_SENDERS = new HashMap<>();

    private final UUID uuid;
    private long lastSequenceNumber;
    private final MicrophonePacketSenderImpl sender;

    public AudioSenderImpl(UUID uuid) {
        this.uuid = uuid;
        this.sender = new MicrophonePacketSenderImpl(this);
    }

    public static boolean registerAudioSender(AudioSenderImpl audioSender) {
        if (Voicechat.SERVER.isCompatible(audioSender.uuid)) {
            return false;
        }
        if (AUDIO_SENDERS.containsKey(audioSender.uuid)) {
            return false;
        }
        AUDIO_SENDERS.put(audioSender.uuid, audioSender);
        return true;
    }

    public static boolean unregisterAudioSender(AudioSenderImpl audioSender) {
        return AUDIO_SENDERS.remove(audioSender.uuid) != null;
    }

    public boolean sendMicrophonePacket(byte[] data, boolean whispering, long sequenceNumber) {
        if (!canSend()) {
            return false;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return true;
        }
        try {
            if (sequenceNumber >= 0L) {
                lastSequenceNumber = sequenceNumber;
            }
            MicPacket packet = new MicPacket(data, whispering, lastSequenceNumber++);
            server.onMicPacket(uuid, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public MicrophonePacketSender microphonePacketSender() {
        return sender;
    }

    @Override
    public boolean canSend() {
        return !Voicechat.SERVER.isCompatible(uuid) && AUDIO_SENDERS.get(uuid) == this;
    }

    public static class MicrophonePacketSenderImpl implements MicrophonePacketSender {

        private final AudioSenderImpl audioSender;
        private byte[] data;
        private boolean whispering;
        private long sequenceNumber;

        public MicrophonePacketSenderImpl(AudioSenderImpl audioSender) {
            this.audioSender = audioSender;
            resetState();
        }

        private void resetState() {
            data = null;
            whispering = false;
            sequenceNumber = -1L;
        }

        @Override
        public MicrophonePacketSender opusEncodedData(byte[] data) {
            this.data = data;
            return this;
        }

        @Override
        public MicrophonePacketSender whispering(boolean whispering) {
            this.whispering = whispering;
            return this;
        }

        @Override
        public MicrophonePacketSender sequenceNumber(long sequenceNumber) {
            if (sequenceNumber < 0L) {
                throw new IllegalArgumentException("Sequence number must be positive");
            }
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        @Override
        public boolean send() {
            boolean success = audioSender.sendMicrophonePacket(data, whispering, sequenceNumber);
            resetState();
            return success;
        }

        @Override
        public boolean reset() {
            boolean success = audioSender.sendMicrophonePacket(new byte[0], false, -1L);
            audioSender.lastSequenceNumber = 0L;
            resetState();
            return success;
        }
    }

}

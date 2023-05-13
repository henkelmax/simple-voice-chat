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
    private boolean whispering;
    private long nextSequenceNumber;

    public AudioSenderImpl(UUID uuid) {
        this.uuid = uuid;
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

    @Override
    public AudioSender whispering(boolean whispering) {
        this.whispering = whispering;
        return this;
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public AudioSender sequenceNumber(long sequenceNumber) {
        if (sequenceNumber < 0L) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }
        this.nextSequenceNumber = sequenceNumber;
        return this;
    }

    @Override
    public boolean canSend() {
        return !Voicechat.SERVER.isCompatible(uuid) && AUDIO_SENDERS.get(uuid) == this;
    }

    @Override
    public boolean send(byte[] opusEncodedAudioData) {
        return sendMicrophonePacket(opusEncodedAudioData);
    }

    @Override
    public boolean reset() {
        return sendMicrophonePacket(new byte[0]);
    }

    public boolean sendMicrophonePacket(byte[] data) {
        if (data == null) {
            throw new IllegalStateException("opusEncodedData is not set");
        }
        if (!canSend()) {
            return false;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return true;
        }
        try {
            MicPacket packet = new MicPacket(data, data.length > 0 && whispering, nextSequenceNumber++);
            if (data.length <= 0) {
                nextSequenceNumber = 0L;
            }
            server.onMicPacket(uuid, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}

package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;

import javax.sound.sampled.AudioFormat;

public class AudioChannelConfig {

    private AudioFormat monoFormat;
    private AudioFormat stereoFormat;
    private int sampleRate;
    private int frameSize;

    public AudioChannelConfig(Client client) {
        sampleRate = 48000;
        frameSize = (sampleRate / 1000) * 2 * 20;
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        Voicechat.LOGGER.info("Setting sample rate to {} Hz, codec to {} and frame size to {} bytes", sampleRate, client.getCodec().name(), frameSize);
    }

    public AudioFormat getMonoFormat() {
        return monoFormat;
    }

    public AudioFormat getStereoFormat() {
        return stereoFormat;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int maxSpeakerBufferSize() {
        return frameSize * (20 + VoicechatClient.CLIENT_CONFIG.outputBufferSize.get());
    }
}

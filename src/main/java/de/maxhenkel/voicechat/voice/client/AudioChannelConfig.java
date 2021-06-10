package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;

import javax.sound.sampled.AudioFormat;

public class AudioChannelConfig {

    private static AudioFormat monoFormat;
    private static AudioFormat stereoFormat;
    private static int sampleRate;
    private static int frameSize;

    public static void onServerConfigUpdate() {
        sampleRate = 48000;
        frameSize = (sampleRate / 1000) * 2 * 20;
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        Main.LOGGER.info("Setting sample rate to {} Hz, codec to {} and frame size to {} bytes", sampleRate, Main.SERVER_CONFIG.voiceChatCodec.get().name(), frameSize);
    }

    public static AudioFormat getMonoFormat() {
        return monoFormat;
    }

    public static AudioFormat getStereoFormat() {
        return stereoFormat;
    }

    public static int getSampleRate() {
        return sampleRate;
    }

    public static int getFrameSize() {
        return frameSize;
    }

    public static int maxSpeakerBufferSize() {
        return frameSize * (32 + Main.CLIENT_CONFIG.outputBufferSize.get());
    }
}

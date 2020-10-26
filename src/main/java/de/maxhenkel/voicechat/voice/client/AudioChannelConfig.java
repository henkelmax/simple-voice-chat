package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;

import javax.sound.sampled.AudioFormat;

public class AudioChannelConfig {

    private static AudioFormat monoFormat;
    private static AudioFormat stereoFormat;
    private static int dataLength;

    public static void onServerConfigUpdate() {
        float sampleRate = Main.SERVER_CONFIG.voiceChatSampleRate.get().floatValue();
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        dataLength = Main.SERVER_CONFIG.voiceChatSampleRate.get() / 4;
        dataLength = dataLength + (dataLength % 2);

        Main.LOGGER.info("Setting sample rate to " + sampleRate + " Hz and data length to " + dataLength + " bytes");
    }

    public static void onClientConfigUpdate() {

    }

    public static AudioFormat getMonoFormat() {
        return monoFormat;
    }

    public static AudioFormat getStereoFormat() {
        return stereoFormat;
    }

    public static int getDataLength() {
        return dataLength;
    }

}

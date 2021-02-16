package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

public class AudioChannelConfig {

    private static AudioFormat monoFormat;
    private static AudioFormat stereoFormat;
    private static int dataLength;
    private static int readSize;

    public static void onServerConfigUpdate() {
        int sampleRate = Main.SERVER_CONFIG.voiceChatSampleRate.get();
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        dataLength = fixAudioFormatSize(Main.SERVER_CONFIG.voiceChatMtuSize.get());
        readSize = fixAudioFormatSize(sampleRate / 16);

        Main.LOGGER.info("Setting sample rate to " + sampleRate + " Hz, mic read size to " + readSize + " and data length to " + dataLength + " bytes");
    }

    public static int fixAudioFormatSize(int size) {
        return size + size % 2;
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

    public static int getReadSize(TargetDataLine line) {
        return Math.min(readSize, fixAudioFormatSize(line.getBufferSize() / 2));
    }

}

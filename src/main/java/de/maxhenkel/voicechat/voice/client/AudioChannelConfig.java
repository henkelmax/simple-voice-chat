package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

public class AudioChannelConfig {

    private AudioFormat monoFormat;
    private AudioFormat stereoFormat;
    private int dataLength;
    private int readSize;

    public AudioChannelConfig(Client client) {
        int sampleRate = client.getSampleRate();
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        dataLength = fixAudioFormatSize(client.getMtuSize());
        readSize = fixAudioFormatSize(sampleRate / 16);

        Voicechat.LOGGER.info("Setting sample rate to " + sampleRate + " Hz, mic read size to " + readSize + " and data length to " + dataLength + " bytes");
    }

    public int fixAudioFormatSize(int size) {
        return size + size % 2;
    }

    public AudioFormat getMonoFormat() {
        return monoFormat;
    }

    public AudioFormat getStereoFormat() {
        return stereoFormat;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getReadSize(TargetDataLine line) {
        return Math.min(readSize, fixAudioFormatSize(line.getBufferSize() / 2));
    }
}

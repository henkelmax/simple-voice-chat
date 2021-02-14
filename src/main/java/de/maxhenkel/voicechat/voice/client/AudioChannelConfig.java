package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;

import javax.sound.sampled.AudioFormat;

public class AudioChannelConfig {

    private AudioFormat monoFormat;
    private AudioFormat stereoFormat;
    private int dataLength;

    public AudioChannelConfig(Client client) {
        float sampleRate = client.getSampleRate();
        monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);

        dataLength = (client.getSampleRate() / 200) * 16;

        Voicechat.LOGGER.info("Setting sample rate to " + sampleRate + " Hz and data length to " + dataLength + " bytes");
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

}

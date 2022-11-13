package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;

import javax.sound.sampled.AudioFormat;

public class AudioChannelConfig {

    public static final AudioFormat MONO_FORMAT;
    public static final AudioFormat STEREO_FORMAT;

    static {
        MONO_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE, 16, 1, 2, SoundManager.SAMPLE_RATE, false);
        STEREO_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE, 16, 2, 4, SoundManager.SAMPLE_RATE, false);
    }

    public static int maxSpeakerBufferSize() {
        return SoundManager.FRAME_SIZE * (32 + VoicechatClient.CLIENT_CONFIG.outputBufferSize.get());
    }
}
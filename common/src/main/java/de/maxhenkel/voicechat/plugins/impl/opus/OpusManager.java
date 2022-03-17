package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.client.SoundManager;

public class OpusManager {

    public static OpusEncoder createEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        NativeOpusEncoderImpl encoder = NativeOpusEncoderImpl.createEncoder(sampleRate, frameSize, maxPayloadSize, application);
        if (encoder != null) {
            return encoder;
        }
        Voicechat.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
        return new JavaOpusEncoderImpl(sampleRate, frameSize, maxPayloadSize, application);
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        int application = ServerConfig.Codec.VOIP.getOpusValue();
        if (mode != null) {
            application = switch (mode) {
                case VOIP -> ServerConfig.Codec.VOIP.getOpusValue();
                case AUDIO -> ServerConfig.Codec.AUDIO.getOpusValue();
                case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY.getOpusValue();
            };
        }
        return createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024, application);
    }

    public static OpusDecoder createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        NativeOpusDecoderImpl decoder = NativeOpusDecoderImpl.createDecoder(sampleRate, frameSize, maxPayloadSize);
        if (decoder != null) {
            return decoder;
        }
        Voicechat.LOGGER.warn("Failed to load native Opus decoder - Falling back to Java Opus implementation");
        return new JavaOpusDecoderImpl(sampleRate, frameSize, maxPayloadSize);
    }

    public static OpusDecoder createDecoder() {
        return createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024);
    }

}

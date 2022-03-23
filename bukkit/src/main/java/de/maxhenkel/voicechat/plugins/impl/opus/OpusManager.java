package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import org.concentus.OpusApplication;

public class OpusManager {

    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = (SAMPLE_RATE / 1000) * 20;

    public static OpusEncoder createEncoder(int sampleRate, int frameSize, int maxPayloadSize, OpusApplication application) {
        return new JavaOpusEncoderImpl(sampleRate, frameSize, maxPayloadSize, application);
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        OpusApplication application = OpusApplication.OPUS_APPLICATION_VOIP;
        if (mode != null) {
            application = switch (mode) {
                case VOIP -> OpusApplication.OPUS_APPLICATION_VOIP;
                case AUDIO -> OpusApplication.OPUS_APPLICATION_AUDIO;
                case RESTRICTED_LOWDELAY -> OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
            };
        }
        return createEncoder(SAMPLE_RATE, FRAME_SIZE, 1024, application);
    }

    public static OpusDecoder createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        return new JavaOpusDecoderImpl(sampleRate, frameSize, maxPayloadSize);
    }

    public static OpusDecoder createDecoder() {
        return createDecoder(SAMPLE_RATE, FRAME_SIZE, 1024);
    }

}

package de.maxhenkel.voicechat.natives;

import de.maxhenkel.opus4j.OpusEncoder.Application;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.plugins.impl.opus.JavaOpusDecoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.JavaOpusEncoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.NativeOpusDecoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.NativeOpusEncoderImpl;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

public class OpusManager extends NativeValidator {

    @Override
    protected void runValidation() throws Throwable {
        NativeOpusEncoderImpl encoder = new NativeOpusEncoderImpl(AudioUtils.SAMPLE_RATE, 1, Application.VOIP);
        encoder.setMaxPayloadSize(AudioUtils.DEFAULT_MAX_PAYLOAD_SIZE);
        byte[] encoded = encoder.encode(new short[AudioUtils.FRAME_SIZE]);
        encoder.resetState();
        encoder.close();

        NativeOpusDecoderImpl decoder = new NativeOpusDecoderImpl(AudioUtils.SAMPLE_RATE, 1);
        decoder.setFrameSize(AudioUtils.FRAME_SIZE);
        decoder.decode(encoded);
        decoder.decode(null);
        decoder.resetState();
        decoder.close();
    }

    @Override
    protected String getNativeName() {
        return "Opus";
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        OpusManager instance = instance();

        int mtuSize = CrossSideManager.get().getMtuSize();

        Application application = Application.VOIP;
        if (mode != null) {
            application = switch (mode) {
                case VOIP -> Application.VOIP;
                case AUDIO -> Application.AUDIO;
                case RESTRICTED_LOWDELAY -> Application.LOW_DELAY;
            };
        }

        if (instance.canUse()) {
            try {
                NativeOpusEncoderImpl encoder = new NativeOpusEncoderImpl(AudioUtils.SAMPLE_RATE, 1, application);
                encoder.setMaxPayloadSize(mtuSize);
                return encoder;
            } catch (Throwable e) {
                instance.setFailed(e.getMessage());
                Voicechat.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
            }
        }

        return new JavaOpusEncoderImpl(AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, mtuSize, application);
    }

    public static OpusDecoder createDecoder() {
        OpusManager instance = instance();

        if (instance.canUse()) {
            try {
                NativeOpusDecoderImpl decoder = new NativeOpusDecoderImpl(AudioUtils.SAMPLE_RATE, 1);
                decoder.setFrameSize(AudioUtils.FRAME_SIZE);
                return decoder;
            } catch (Throwable e) {
                instance.setFailed(e.getMessage());
                Voicechat.LOGGER.warn("Failed to load native Opus decoder - Falling back to Java Opus implementation");
            }
        }
        return new JavaOpusDecoderImpl(AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE);
    }

    public static void init() {
        instance().initialize();
    }

    public static boolean isFailed() {
        return !instance().canUse();
    }

    public static String getFailedMessage() {
        return instance().getMessage();
    }

    private static OpusManager instance;

    private static synchronized OpusManager instance() {
        if (instance == null) {
            instance = new OpusManager();
        }
        return instance;
    }

}

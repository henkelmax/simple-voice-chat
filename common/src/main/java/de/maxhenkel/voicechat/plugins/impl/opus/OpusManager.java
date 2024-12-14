package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.OpusEncoder.Application;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;

public class OpusManager {

    private static boolean nativeOpusCompatible = true;

    public static boolean opusNativeCheck() {
        Voicechat.LOGGER.info("Loading Opus");
        if (!nativeOpusCompatible || !CrossSideManager.get().useNatives()) {
            return false;
        }

        Boolean success = Utils.createSafe(() -> {
            NativeOpusEncoderImpl encoder = new NativeOpusEncoderImpl(Utils.SAMPLE_RATE, 1, Application.VOIP);
            encoder.setMaxPayloadSize(Utils.DEFAULT_MAX_PAYLOAD_SIZE);
            byte[] encoded = encoder.encode(new short[Utils.FRAME_SIZE]);
            encoder.resetState();
            encoder.close();

            NativeOpusDecoderImpl decoder = new NativeOpusDecoderImpl(Utils.SAMPLE_RATE, 1);
            decoder.setFrameSize(Utils.FRAME_SIZE);
            decoder.decode(encoded);
            decoder.decodeFec();
            decoder.resetState();
            decoder.close();
            return true;
        }, e -> {
            Voicechat.LOGGER.warn("Failed to load native Opus implementation", e);
        });
        if (success == null || !success) {
            Voicechat.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
            nativeOpusCompatible = false;
            return false;
        }
        return true;
    }

    @Nullable
    private static OpusEncoder createNativeEncoder(int mtuSize, Application application) {
        if (!nativeOpusCompatible) {
            return null;
        }

        try {
            NativeOpusEncoderImpl encoder = new NativeOpusEncoderImpl(Utils.SAMPLE_RATE, 1, application);
            encoder.setMaxPayloadSize(mtuSize);
            return encoder;
        } catch (Throwable e) {
            nativeOpusCompatible = false;
            Voicechat.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
            return null;
        }
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        int mtuSize = CrossSideManager.get().getMtuSize();

        Application application = Application.VOIP;
        if (mode != null) {
            switch (mode) {
                case VOIP:
                    application = Application.VOIP;
                    break;
                case AUDIO:
                    application = Application.AUDIO;
                    break;
                case RESTRICTED_LOWDELAY:
                    application = Application.LOW_DELAY;
                    break;
            }
        }

        if (CrossSideManager.get().useNatives()) {
            OpusEncoder encoder = createNativeEncoder(mtuSize, application);
            if (encoder != null) {
                return encoder;
            }
        }

        return new JavaOpusEncoderImpl(Utils.SAMPLE_RATE, Utils.FRAME_SIZE, mtuSize, application);
    }

    @Nullable
    private static OpusDecoder createNativeDecoder() {
        if (!nativeOpusCompatible) {
            return null;
        }
        try {
            NativeOpusDecoderImpl decoder = new NativeOpusDecoderImpl(Utils.SAMPLE_RATE, 1);
            decoder.setFrameSize(Utils.FRAME_SIZE);
            return decoder;
        } catch (Throwable e) {
            nativeOpusCompatible = false;
            Voicechat.LOGGER.warn("Failed to load native Opus decoder - Falling back to Java Opus implementation");
            return null;
        }
    }

    public static OpusDecoder createDecoder() {
        if (CrossSideManager.get().useNatives()) {
            OpusDecoder decoder = createNativeDecoder();
            if (decoder != null) {
                return decoder;
            }
        }
        return new JavaOpusDecoderImpl(Utils.SAMPLE_RATE, Utils.FRAME_SIZE);
    }

}

package de.maxhenkel.voicechat.natives;

import de.maxhenkel.lame4j.Mp3Decoder;
import de.maxhenkel.lame4j.Mp3Encoder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.*;

public class LameManager extends NativeValidator {

    @Override
    protected void runValidation() throws Throwable {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (Mp3Encoder encoder = new Mp3Encoder(1, AudioUtils.SAMPLE_RATE, 128, 5, byteArrayOutputStream)) {
                encoder.write(new short[AudioUtils.FRAME_SIZE]);
            }
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                try (Mp3Decoder decoder = new Mp3Decoder(byteArrayInputStream)) {
                    decoder.decodeNextFrame();
                    decoder.getSampleRate();
                    decoder.getBitRate();
                    decoder.getChannelCount();
                }
            }
        }
    }

    @Override
    protected String getNativeName() {
        return "LAME";
    }

    @Nullable
    public static Mp3EncoderImpl createEncoder(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) {
        LameManager instance = instance();
        if (!instance.canUse()) {
            return null;
        }
        return NativeUtils.createSafe(() -> new Mp3EncoderImpl(audioFormat, bitrate, quality, outputStream), e -> {
            instance.setFailed(e.getMessage());
            Voicechat.LOGGER.warn("Failed to load LAME encoder", e);
        });
    }

    @Nullable
    public static Mp3Decoder createDecoder(InputStream inputStream) {
        LameManager instance = instance();
        if (!instance.canUse()) {
            return null;
        }
        return NativeUtils.createSafe(() -> new Mp3Decoder(inputStream), e -> {
            instance.setFailed(e.getMessage());
            Voicechat.LOGGER.warn("Failed to load LAME decoder", e);
        });
    }

    public static boolean canUseLame() {
        return instance().canUse();
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

    private static LameManager instance;

    private static synchronized LameManager instance() {
        if (instance == null) {
            instance = new LameManager();
        }
        return instance;
    }

}

package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.Opus;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpusManager {

    private static Boolean nativeOpusCompatible;

    public static boolean isNativeOpusCompatible() {
        if (nativeOpusCompatible == null) {
            Boolean isCompatible = Utils.createSafe(OpusManager::isOpusCompatible);
            nativeOpusCompatible = isCompatible != null && isCompatible;
        }
        return nativeOpusCompatible;
    }

    public static OpusEncoder createEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        if (isNativeOpusCompatible()) {
            NativeOpusEncoderImpl encoder = NativeOpusEncoderImpl.createEncoder(sampleRate, frameSize, maxPayloadSize, application);
            if (encoder != null) {
                return encoder;
            }
            nativeOpusCompatible = false;
            Voicechat.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
        }
        return new JavaOpusEncoderImpl(sampleRate, frameSize, maxPayloadSize, application);
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        int application = ServerConfig.Codec.VOIP.getOpusValue();
        if (mode != null) {
            switch (mode) {
                case VOIP:
                    application = ServerConfig.Codec.VOIP.getOpusValue();
                    break;
                case AUDIO:
                    application = ServerConfig.Codec.AUDIO.getOpusValue();
                    break;
                case RESTRICTED_LOWDELAY:
                    application = ServerConfig.Codec.RESTRICTED_LOWDELAY.getOpusValue();
                    break;
            }
        }
        return createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024, application);
    }

    public static OpusDecoder createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        if (isNativeOpusCompatible()) {
            NativeOpusDecoderImpl decoder = NativeOpusDecoderImpl.createDecoder(sampleRate, frameSize, maxPayloadSize);
            if (decoder != null) {
                return decoder;
            }
            nativeOpusCompatible = false;
            Voicechat.LOGGER.warn("Failed to load native Opus decoder - Falling back to Java Opus implementation");
        }
        return new JavaOpusDecoderImpl(sampleRate, frameSize, maxPayloadSize);
    }

    public static OpusDecoder createDecoder() {
        return createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024);
    }

    public static Pattern VERSIONING_PATTERN = Pattern.compile("^[^\\d\\.]* ?(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)){0,1}){0,1}.*$");

    private static boolean isOpusCompatible() {
        String versionString = Opus.INSTANCE.opus_get_version_string();

        Matcher matcher = VERSIONING_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            Voicechat.LOGGER.warn("Failed to parse Opus version '{}'", versionString);
            return false;
        }
        String majorGroup = matcher.group("major");
        String minorGroup = matcher.group("minor");
        String patchGroup = matcher.group("patch");
        int actualMajor = majorGroup == null ? 0 : Integer.parseInt(majorGroup);
        int actualMinor = minorGroup == null ? 0 : Integer.parseInt(minorGroup);
        int actualPatch = patchGroup == null ? 0 : Integer.parseInt(patchGroup);

        if (!isMinimum(actualMajor, actualMinor, actualPatch, 1, 1, 0)) {
            Voicechat.LOGGER.warn("Outdated Opus version detected: {}", versionString);
            return false;
        }

        Voicechat.LOGGER.info("Using Opus version '{}'", versionString);
        return true;
    }

    private static boolean isMinimum(int actualMajor, int actualMinor, int actualPatch, int major, int minor, int patch) {
        if (major > actualMajor) {
            return false;
        } else if (major == actualMajor) {
            if (minor > actualMinor) {
                return false;
            } else if (minor == actualMinor) {
                return patch <= actualPatch;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}

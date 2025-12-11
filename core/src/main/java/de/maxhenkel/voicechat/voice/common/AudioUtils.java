package de.maxhenkel.voicechat.voice.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioUtils {

    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = (SAMPLE_RATE / 1000) * 20;
    public static final int DEFAULT_MAX_PAYLOAD_SIZE = 1024;
    public static final double LOWEST_DB = -127D;

    public static short[] bytesToShorts(byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Input bytes need to be divisible by 2");
        }
        ShortBuffer sb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] out = new short[sb.remaining()];
        sb.get(out);
        return out;
    }

    public static byte[] shortsToBytes(short[] shorts) {
        ByteBuffer bb = ByteBuffer.allocate(shorts.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (short s : shorts) {
            bb.putShort(s);
        }
        return bb.array();
    }

    private static final float FLOAT_SHORT_SCALE = Short.MAX_VALUE;
    private static final float FLOAT_SHORT_SCALING_FACTOR = 1F / FLOAT_SHORT_SCALE;
    private static final float FLOAT_CLIP = FLOAT_SHORT_SCALE - 1;

    public static short[] floatsToShortsNormalized(float[] audioData) {
        short[] shortAudioData = new short[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            shortAudioData[i] = (short) Math.max(Math.min(audioData[i] * FLOAT_SHORT_SCALE, FLOAT_CLIP), -FLOAT_SHORT_SCALE);
        }
        return shortAudioData;
    }

    public static float[] shortsToFloatsNormalized(short[] audioData) {
        float[] floatAudioData = new float[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            floatAudioData[i] = (float) audioData[i] * FLOAT_SHORT_SCALING_FACTOR;
        }
        return floatAudioData;
    }

    public static short[] floatsToShorts(float[] floats) {
        float max = Short.MIN_VALUE;
        float min = Short.MAX_VALUE;
        for (int i = 0; i < floats.length; i++) {
            if (floats[i] > max) {
                max = floats[i];
            }
            if (floats[i] < min) {
                min = floats[i];
            }
        }

        float scale = Math.min(1F, FLOAT_CLIP / Math.max(Math.abs(max), Math.abs(min)));

        short[] shorts = new short[floats.length];
        for (int i = 0; i < floats.length; i++) {
            shorts[i] = ((Float) (floats[i] * scale)).shortValue();
        }
        return shorts;
    }

    public static float[] shortsToFloats(short[] shorts) {
        float[] floats = new float[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            floats[i] = ((Short) shorts[i]).floatValue();
        }
        return floats;
    }

    public static byte[] floatsToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i = 0; i < floats.length; i++) {
            short x = ((Float) floats[i]).shortValue();
            bytes[i * 2] = (byte) (x & 0x00FF);
            bytes[i * 2 + 1] = (byte) ((x & 0xFF00) >> 8);
        }
        return bytes;
    }

    public static float[] bytesToFloats(byte[] bytes) {
        float[] floats = new float[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++) {
            if ((bytes[i * 2 + 1] & 0x80) != 0) {
                floats[i] = Short.MIN_VALUE + ((bytes[i * 2 + 1] & 0x7F) << 8) | (bytes[i * 2] & 0xFF);
            } else {
                floats[i] = ((bytes[i * 2 + 1] << 8) & 0xFF00) | (bytes[i * 2] & 0xFF);
            }
        }
        return floats;
    }

    /**
     * Calculates the highest audio level of this frame
     *
     * @param samples the audio samples
     * @return the audio level in db
     */
    public static double getHighestAudioLevel(short[] samples) {
        short highest = 0;
        for (short sample : samples) {
            if (Math.abs(sample) > highest) {
                highest = (short) Math.abs(sample);
            }
        }
        return sampleDb(highest);
    }

    /**
     * Converts a peak dBFS level to the SpeexDSP AGC target/level value.
     * <p>
     * Mapping: target = 32768 * 10^(dBFS/20)
     * (0 dBFS corresponds to full-scale 16-bit peak 32768)
     *
     * @param dbfs target level in dBFS (e.g., -12.0, -8.0, -5.0)
     * @return integer AGC target in the valid Speex range [1, 32768]
     */
    public static int dbSample(double dbfs) {
        double value = 32768D * Math.pow(10D, dbfs / 20D);
        long rounded = Math.round(value);
        if (rounded < 0) {
            return 0;
        }
        if (rounded > 32768) {
            return 32768;
        }
        return (int) rounded;
    }

    public static double sampleDb(short sample) {
        if (sample == 0) {
            return LOWEST_DB;
        }
        int mag = Math.abs(sample);
        double norm = mag / 32768D;

        double db = 20D * Math.log10(norm);

        if (!Double.isFinite(db)) {
            return LOWEST_DB;
        }
        if (db > 0D) {
            db = 0D;
        }
        if (db < LOWEST_DB) {
            db = LOWEST_DB;
        }
        return db;
    }

    /**
     * Convert gain in dB to linear multiplier (+6 dB ~ 1.995, -6 dB ~ 0.501).
     *
     * @param db the gain in dB
     * @return the multiplier
     */
    public static double dbToLinear(double db) {
        return Math.pow(10D, db / 20D);
    }

    /**
     * Convert linear multiplier to gain in dB.
     *
     * @param multiplier the multiplier
     * @return the gain in dB - bottoms out at {@link AudioUtils#LOWEST_DB}
     */
    public static double linearToDb(double multiplier) {
        if (multiplier < 0.001D) {
            return LOWEST_DB;
        }
        return 20D * Math.log10(multiplier);
    }

    /**
     * @param samples   the audio samples
     * @param threshold the activation threshold
     * @return if the audio level was above the threshold
     */
    public static boolean isAboveThreshold(short[] samples, double threshold) {
        return getHighestAudioLevel(samples) > threshold;
    }

    public static short[] combineAudio(Iterable<short[]> audioParts) {
        short[] result = new short[FRAME_SIZE];
        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = 0;
            for (short[] audio : audioParts) {
                if (audio == null) {
                    sample += 0;
                } else {
                    sample += audio[i];
                }
            }
            if (sample > Short.MAX_VALUE) {
                result[i] = Short.MAX_VALUE;
            } else if (sample < Short.MIN_VALUE) {
                result[i] = Short.MIN_VALUE;
            } else {
                result[i] = (short) sample;
            }
        }
        return result;
    }

    /**
     * Converts a dB value to a percentage value ({@link AudioUtils#LOWEST_DB} - 0) - (0 - 1)
     *
     * @param db the decibel value
     * @return the percentage
     */
    public static double dbToPerc(double db) {
        db = Math.min(Math.max(db, AudioUtils.LOWEST_DB), 0D);
        return (db + Math.abs(AudioUtils.LOWEST_DB)) / Math.abs(AudioUtils.LOWEST_DB);
    }

    /**
     * Converts a percentage to a dB value (0 - 1) - ({@link AudioUtils#LOWEST_DB} - 0)
     *
     * @param perc the percentage
     * @return the decibel value
     */
    public static double percToDb(double perc) {
        perc = Math.min(Math.max(perc, 0D), 1D);
        return (1D - perc) * AudioUtils.LOWEST_DB;
    }

}

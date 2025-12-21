package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.voice.common.AudioUtils;

import java.util.Arrays;

public class VolumeManager {

    public static final double MIN_GAIN = -40D;
    public static final double MAX_GAIN = 24D;
    private static final short MAX_AMPLIFICATION = Short.MAX_VALUE - 1;

    private final double[] maxVolumes;
    private int index;

    public VolumeManager() {
        maxVolumes = new double[50];
        Arrays.fill(maxVolumes, -1D);
    }

    /**
     * Changes the volume of 16-bit mono audio.
     * Note that this modifies the input array in place.
     *
     * @param audio  the audio data
     * @param gainDb the gain in dB
     */
    public void adjustVolume(short[] audio, double gainDb) {
        double maxMultiplier;
        if (gainDb <= MIN_GAIN) {
            maxMultiplier = 0D;
        } else {
            maxMultiplier = AudioUtils.dbToLinear(gainDb);
        }
        maxVolumes[index] = getMaximumMultiplier(audio, maxMultiplier);
        index = (index + 1) % maxVolumes.length;
        double min = -1D;
        for (double mul : maxVolumes) {
            if (mul < 0D) {
                continue;
            }
            if (min < 0D) {
                min = mul;
                continue;
            }
            if (mul < min) {
                min = mul;
            }
        }

        double maxVolume = Math.min(min, maxMultiplier);

        for (int i = 0; i < audio.length; i++) {
            audio[i] = (short) ((double) audio[i] * maxVolume);
        }
    }

    private static double getMaximumMultiplier(short[] audio, double multiplier) {
        short max = 0;

        for (short value : audio) {
            short abs;
            if (value <= Short.MIN_VALUE) {
                abs = (short) Math.abs(value + 1);
            } else {
                abs = (short) Math.abs(value);
            }
            if (abs > max) {
                max = abs;
            }
        }

        if (max == 0) {
            return multiplier;
        }
        return Math.min(multiplier, (double) MAX_AMPLIFICATION / (double) max);
    }

}

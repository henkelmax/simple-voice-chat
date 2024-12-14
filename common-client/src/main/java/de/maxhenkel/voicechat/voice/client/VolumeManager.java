package de.maxhenkel.voicechat.voice.client;

import java.util.Arrays;

public class VolumeManager {

    private static final short MAX_AMPLIFICATION = Short.MAX_VALUE - 1;

    private final float[] maxVolumes;
    private int index;

    public VolumeManager() {
        maxVolumes = new float[50];
        Arrays.fill(maxVolumes, -1F);
    }

    /**
     * Changes the volume of 16 bit audio
     * Note that this modifies the input array
     *
     * @param audio  the audio data
     * @param volume the amplification
     * @return the adjusted audio
     */
    public short[] adjustVolumeMono(short[] audio, float volume) {
        maxVolumes[index] = getMaximumMultiplier(audio, volume);
        index = (index + 1) % maxVolumes.length;
        float min = -1F;
        for (float mul : maxVolumes) {
            if (mul < 0F) {
                continue;
            }
            if (min < 0F) {
                min = mul;
                continue;
            }
            if (mul < min) {
                min = mul;
            }
        }

        float maxVolume = Math.min(min, volume);

        for (int i = 0; i < audio.length; i++) {
            audio[i] = (short) ((float) audio[i] * maxVolume);
        }
        return audio;
    }

    private static float getMaximumMultiplier(short[] audio, float multiplier) {
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

        return Math.min(multiplier, (float) MAX_AMPLIFICATION / (float) max);
    }

}

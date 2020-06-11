package de.maxhenkel.voicechat.voice.common;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }

    /**
     * Decompresses the given data with the gzip algorithm
     *
     * @param data the data to decompress
     * @return the decompressed data
     * @throws IOException if an IO error occurs
     */
    public static byte[] gUnzip(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gzipIS = new GZIPInputStream(bis);

        byte[] buffer = new byte[128];
        int len;
        while ((len = gzipIS.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.flush();
        byte[] decompressed = bos.toByteArray();

        gzipIS.close();
        bos.close();
        bis.close();
        return decompressed;
    }

    /**
     * Compresses the given bytes with the gzip algorithm
     *
     * @param data the data to compress
     * @return the compressed data
     * @throws IOException if an IO error occurs
     */
    public static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzipOut = new GZIPOutputStream(bos);
        gzipOut.write(data);
        gzipOut.flush();
        gzipOut.close();
        bos.flush();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    /**
     * Changes the volume of 16 bit audio
     * Note that this modifies the input array
     *
     * @param audio  the audio data
     * @param volume the amplification
     * @return the adjusted audio
     */
    public static byte[] adjustVolumeMono(byte[] audio, float volume) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));

            audioSample = (short) (audioSample * volume);

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);

        }
        return audio;
    }

    /**
     * Changes the volume of 16 bit audio
     * Note that this modifies the input array
     *
     * @param audio       the audio data
     * @param volumeLeft  the amplification of the left audio
     * @param volumeRight the amplification of the right audio
     * @return the adjusted audio
     */
    public static byte[] adjustVolumeStereo(byte[] audio, float volumeLeft, float volumeRight) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));

            audioSample = (short) (audioSample * (i % 4 == 0 ? volumeLeft : volumeRight));

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);

        }
        return audio;
    }

    /**
     * Convorts 16 bit mono audio to stereo
     *
     * @param audio the audio data
     * @return the adjusted audio
     */
    public static byte[] convertToStereo(byte[] audio) {
        byte[] stereo = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i += 2) {
            stereo[i * 2] = audio[i];
            stereo[i * 2 + 1] = audio[i + 1];

            stereo[i * 2 + 2] = audio[i];
            stereo[i * 2 + 3] = audio[i + 1];
        }
        return stereo;
    }

    /**
     * Convorts 16 bit mono audio to stereo
     *
     * @param audio       the audio data
     * @param volumeLeft  the volume modifier for the left audio
     * @param volumeRight tthe volume modifier for the right audio
     * @return the adjusted audio
     */
    public static byte[] convertToStereo(byte[] audio, float volumeLeft, float volumeRight) {
        byte[] stereo = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));
            short left = (short) (audioSample * volumeLeft);
            short right = (short) (audioSample * volumeRight);
            stereo[i * 2] = (byte) left;
            stereo[i * 2 + 1] = (byte) (left >> 8);

            stereo[i * 2 + 2] = (byte) right;
            stereo[i * 2 + 3] = (byte) (right >> 8);
        }
        return stereo;
    }

    public static Pair<Float, Float> getStereoVolume(Vec3d playerPos, float yaw, Vec3d soundPos) {
        Vec3d d = soundPos.subtract(playerPos).normalize();
        Vec2f diff = new Vec2f((float) d.x, (float) d.z);
        float diffAngle = angle(diff, new Vec2f(-1F, 0F));

        float angle = normalizeAngle(diffAngle - (yaw % 360F));

        float rot = angle / 180F;
        float perc = rot;
        if (rot < -0.5F) {
            perc = -(0.5F + (rot + 0.5F));
        } else if (rot > 0.5F) {
            perc = 0.5F - (rot - 0.5F);
        }

        float left = Math.max(0.3F, perc < 0F ? Math.abs(perc * 2F) : 0F);
        float right = Math.max(0.3F, perc >= 0F ? perc * 2F : 0F);

        float fill;
        if (left > right) {
            fill = 1F - left;
        } else {
            fill = 1F - right;
        }
        left += fill;
        right += fill;
        return new ImmutablePair<>(left, right);
    }

    private static float normalizeAngle(float angle) {
        angle = angle % 360F;
        if (angle <= -180F) {
            angle += 360F;
        } else if (angle > 180F) {
            angle -= 360F;
        }
        return angle;
    }

    private static float angle(Vec2f vec1, Vec2f vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }

    private static float magnitude(Vec2f vec1) {
        return MathHelper.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2));
    }

    private static float multiply(Vec2f vec1, Vec2f vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    private static Vec2f rotate(Vec2f vec, float angle) {
        return new Vec2f(vec.x * MathHelper.cos(angle) - vec.y * MathHelper.sin(angle), vec.x * MathHelper.sin(angle) + vec.y * MathHelper.cos(angle));
    }

}

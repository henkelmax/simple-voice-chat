package de.maxhenkel.voicechat.voice.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioUtils {

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

    public static short[] floatsToShorts(float[] floats) {
        short[] shorts = new short[floats.length];
        for (int i = 0; i < floats.length; i++) {
            shorts[i] = ((Float) floats[i]).shortValue();
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

}

package de.maxhenkel.voicechat.util;

import java.nio.ByteBuffer;

public class VarIntUtils {

    private static final int MAX_VARINT_SIZE = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int read(ByteBuffer buf) {
        int result = 0;
        int bytesRead = 0;

        byte b;
        do {
            b = buf.get();
            result |= (b & DATA_BITS_MASK) << bytesRead++ * DATA_BITS_PER_BYTE;
            if (bytesRead > MAX_VARINT_SIZE) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b & CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK);

        return result;
    }

    public static ByteBuffer write(ByteBuffer buf, int varint) {
        while ((varint & -CONTINUATION_BIT_MASK) != 0) {
            buf.put((byte) (varint & DATA_BITS_MASK | CONTINUATION_BIT_MASK));
            varint >>>= DATA_BITS_PER_BYTE;
        }

        buf.put((byte) varint);
        return buf;
    }

}

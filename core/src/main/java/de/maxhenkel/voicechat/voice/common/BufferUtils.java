package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BufferUtils {

    public static UUID readUUID(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUUID(ByteBuf buf, UUID id) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }

    public static String readUtf(ByteBuf buf) {
        return readUtf(buf, 32767);
    }

    public static String readUtf(ByteBuf buf, int i) {
        int j = getMaxEncodedUtfLength(i);
        int k = readVarInt(buf);
        if (k > j) {
            throw new RuntimeException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
        } else if (k < 0) {
            throw new RuntimeException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String string = buf.toString(buf.readerIndex(), k, StandardCharsets.UTF_8);
            buf.readerIndex(buf.readerIndex() + k);
            if (string.length() > i) {
                int var10002 = string.length();
                throw new RuntimeException("The received string length is longer than maximum allowed (" + var10002 + " > " + i + ")");
            } else {
                return string;
            }
        }
    }

    public static void writeUtf(ByteBuf buf, String string) {
        writeUtf(buf, string, 32767);
    }

    public static void writeUtf(ByteBuf buf, String string, int i) {
        if (string.length() > i) {
            int var10002 = string.length();
            throw new RuntimeException("String too big (was " + var10002 + " characters, max " + i + ")");
        } else {
            byte[] bs = string.getBytes(StandardCharsets.UTF_8);
            int j = getMaxEncodedUtfLength(i);
            if (bs.length > j) {
                throw new RuntimeException("String too big (was " + bs.length + " bytes encoded, max " + j + ")");
            } else {
                writeVarInt(buf, bs.length);
                buf.writeBytes(bs);
            }
        }
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, buf.readableBytes());
    }

    public static void writeByteArray(ByteBuf buf, byte[] bytes) {
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    private static int getMaxEncodedUtfLength(int i) {
        return i * 3;
    }

    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buf.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }


    public static byte[] readByteArray(ByteBuf buf, int i) {
        int j = readVarInt(buf);
        if (j > i) {
            throw new RuntimeException("ByteArray with size " + j + " is bigger than allowed " + i);
        } else {
            byte[] bs = new byte[j];
            buf.readBytes(bs);
            return bs;
        }
    }

    public static void writeVarInt(ByteBuf buf, int i) {
        while((i & -128) != 0) {
            buf.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        buf.writeByte(i);
    }
}

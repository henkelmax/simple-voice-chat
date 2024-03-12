package de.maxhenkel.voicechat.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteBufferWrapper {

    protected ByteBuffer buffer;

    public ByteBufferWrapper(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean readBoolean() {
        return buffer.get() != (byte) 0;
    }

    public void writeBoolean(boolean b) {
        buffer.put(b ? (byte) 1 : (byte) 0);
    }

    public byte readByte() {
        return buffer.get();
    }

    public void writeByte(byte b) {
        buffer.put(b);
    }

    public int readInt() {
        return buffer.getInt();
    }

    public void writeInt(int i) {
        buffer.putInt(i);
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    public void writeDouble(double d) {
        buffer.putDouble(d);
    }

    public UUID readUUID() {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public void writeUUID(UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }

    public byte[] readByteArray() {
        int size = readVarInt();
        return readByteArray(size);
    }

    protected byte[] readByteArray(int size) {
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return bytes;
    }

    public void writeByteArray(byte[] bytes) {
        writeVarInt(bytes.length);
        buffer.put(bytes);
    }

    public String readUtf() {
        return readUtf(32767);
    }

    public void writeUtf(String string) {
        writeUtf(string, 32767);
    }

    public String readUtf(int i) {
        int stringByteLength = readVarInt();
        if (stringByteLength > i * 4) {
            throw new RuntimeException("The read string is larger than the allowed size");
        } else if (stringByteLength < 0) {
            throw new RuntimeException("Received string with negative length");
        }

        byte[] stringBytes = readByteArray(stringByteLength);
        String string = new String(stringBytes, StandardCharsets.UTF_8);
        if (string.length() > i) {
            throw new RuntimeException("The read string is larger than the allowed size");
        }
        return string;
    }

    public void writeUtf(String string, int i) {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        if (stringBytes.length > i) {
            throw new RuntimeException("Trying to write string with a length larger than the allowed maximum");
        } else {
            writeByteArray(stringBytes);
        }
    }

    public int readVarInt() {
        return VarIntUtils.read(buffer);
    }

    public void writeVarInt(int i) {
        VarIntUtils.write(buffer, i);
    }

}

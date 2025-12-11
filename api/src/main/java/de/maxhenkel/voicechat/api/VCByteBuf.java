package de.maxhenkel.voicechat.api;

import java.util.UUID;

public interface VCByteBuf {
    Object getBuffer();

    int readableBytes();

    boolean readBoolean();

    void writeBoolean(boolean b);

    byte readByte();

    void writeByte(int b);

    short readShort();

    void writeShort(short s);

    int readInt();

    void writeInt(int i);

    long readLong();

    void writeLong(long l);

    float readFloat();

    void writeFloat(float f);

    double readDouble();

    void writeDouble(double d);

    UUID readUUID();

    void writeUUID(UUID id);

    String readUtf(int i);

    void writeUtf(String string, int i);

    void writeUtf(String string);

    byte[] readByteArray();

    void readBytes(byte[] bytes);

    void writeByteArray(byte[] bytes);

    void writeBytes(byte[] bytes);
}

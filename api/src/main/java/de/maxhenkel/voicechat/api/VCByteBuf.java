package de.maxhenkel.voicechat.api;

import java.util.UUID;

public interface VCByteBuf {
    Object getBuffer();

    String readUtf(int i);

    void writeUtf(String categoryId, int i);

    byte[] readByteArray();

    UUID readUUID();

    long readLong();

    void writeUUID(UUID id);

    void writeLong(long timestamp);

    int readableBytes();

    void readBytes(byte[] response);

    boolean readBoolean();

    short readShort();

    void writeBoolean(boolean b);

    void writeShort(short anInt);

    int readInt();

    void writeInt(int size);

    byte readByte();

    double readDouble();

    void writeByte(int ordinal);

    void writeDouble(double voiceChatDistance);

    void writeUtf(String voiceHost);

    void writeByteArray(byte[] data);

    float readFloat();

    void writeFloat(float distance);

    void writeBytes(byte[] secret);
}

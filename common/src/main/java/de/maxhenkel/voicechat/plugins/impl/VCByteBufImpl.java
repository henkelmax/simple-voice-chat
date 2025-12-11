package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.VCByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class VCByteBufImpl implements VCByteBuf {

    protected FriendlyByteBuf buf;

    public VCByteBufImpl(ByteBuf buf) {
        this.buf = new FriendlyByteBuf(buf);
    }

    @Override
    public Object getBuffer() {
        return buf;
    }

    @Override
    public String readUtf(int i) {
        return buf.readUtf();
    }

    @Override
    public void writeUtf(String categoryId, int i) {
        buf.writeUtf(categoryId, i);
    }

    @Override
    public byte[] readByteArray() {
        return buf.readByteArray();
    }

    @Override
    public UUID readUUID() {
        return buf.readUUID();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public void writeUUID(UUID id) {
        buf.writeUUID(id);
    }

    @Override
    public void writeLong(long timestamp) {
        buf.writeLong(timestamp);
    }

    @Override
    public int readableBytes() {
        return buf.readableBytes();
    }

    @Override
    public void readBytes(byte[] response) {
        buf.readBytes(response);
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public void writeBoolean(boolean b) {
        buf.writeBoolean(b);
    }

    @Override
    public void writeShort(short anInt) {
        buf.writeShort(anInt);
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public void writeInt(int size) {
        buf.writeInt(size);
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public void writeByte(int ordinal) {
        buf.writeByte(ordinal);
    }

    @Override
    public void writeDouble(double voiceChatDistance) {
        buf.writeDouble(voiceChatDistance);
    }

    @Override
    public void writeUtf(String voiceHost) {
        buf.writeUtf(voiceHost);
    }

    @Override
    public void writeByteArray(byte[] data) {
        buf.writeByteArray(data);
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public void writeFloat(float distance) {
        buf.writeFloat(distance);
    }

    @Override
    public void writeBytes(byte[] secret) {
        buf.writeBytes(secret);
    }
}

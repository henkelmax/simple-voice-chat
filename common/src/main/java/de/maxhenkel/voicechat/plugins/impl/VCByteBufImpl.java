package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.VCByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
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
    public int readableBytes() {
        return buf.readableBytes();
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public void writeBoolean(boolean b) {
        buf.writeBoolean(b);
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public void writeByte(int i) {
        buf.writeByte(i);
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public void writeShort(short s) {
        buf.writeShort(s);
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public void writeInt(int i) {
        buf.writeInt(i);
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public void writeLong(long l) {
        buf.writeLong(l);
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public void writeFloat(float f) {
        buf.writeFloat(f);
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public void writeDouble(double d) {
        buf.writeDouble(d);
    }

    @Override
    public UUID readUUID() {
        return buf.readUUID();
    }

    @Override
    public void writeUUID(UUID id) {
        buf.writeUUID(id);
    }

    @Override
    public String readUtf(int i) {
        return buf.readUtf(i);
    }

    @Override
    public void writeUtf(String string, int i) {
        buf.writeUtf(string, i);
    }

    @Override
    public void writeUtf(String string) {
        buf.writeUtf(string);
    }
    @Override
    public byte[] readByteArray() {
        return buf.readByteArray();
    }

    @Override
    public void readBytes(byte[] bytes) {
        buf.readBytes(bytes);
    }

    @Override
    public void writeByteArray(byte[] bytes) {
        buf.writeByteArray(bytes);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        buf.writeBytes(bytes);
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof VCByteBuf buf1)) {
            return false;
        }
        return Objects.equals(buf, buf1.getBuffer());
    }

    @Override
    public int hashCode() {
        return buf != null ? buf.hashCode() : 0;
    }
}

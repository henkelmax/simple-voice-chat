package de.maxhenkel.voicechat.util;

import io.netty.buffer.*;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FriendlyByteBuf extends ByteBuf {

    private final ByteBuf buf;

    public FriendlyByteBuf(ByteBuf byteBuf) {
        buf = byteBuf;
    }

    public FriendlyByteBuf() {
        this(Unpooled.buffer());
    }

    public ByteBuf getUnderlyingByteBuf() {
        return buf;
    }

    public FriendlyByteBuf writeByteArray(byte[] bs) {
        writeVarInt(bs.length);
        writeBytes(bs);
        return this;
    }

    public byte[] readByteArray() {
        return readByteArray(readableBytes());
    }

    public byte[] readByteArray(int i) {
        int j = readVarInt();
        if (j > i) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
        } else {
            byte[] bs = new byte[j];
            readBytes(bs);
            return bs;
        }
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b & 128) == 128);

        return i;
    }

    public FriendlyByteBuf writeUUID(UUID uUID) {
        writeLong(uUID.getMostSignificantBits());
        writeLong(uUID.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public FriendlyByteBuf writeVarInt(int i) {
        while ((i & -128) != 0) {
            writeByte(i & 127 | 128);
            i >>>= 7;
        }

        writeByte(i);
        return this;
    }

    public String readUtf(int i) {
        int j = readVarInt();
        if (j > i * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i * 4 + ')');
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String string = toString(readerIndex(), j, StandardCharsets.UTF_8);
            readerIndex(readerIndex() + j);
            if (string.length() > i) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + i + ')');
            } else {
                return string;
            }
        }
    }

    public FriendlyByteBuf writeUtf(String string, int i) {
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        if (bs.length > i) {
            throw new EncoderException("String too big (was " + bs.length + " bytes encoded, max " + i + ')');
        } else {
            writeVarInt(bs.length);
            writeBytes(bs);
            return this;
        }
    }

    public String readUtf() {
        return readUtf(32767);
    }

    public FriendlyByteBuf writeUtf(String string) {
        return writeUtf(string, 32767);
    }

    @Override
    public int capacity() {
        return buf.capacity();
    }

    @Override
    public ByteBuf capacity(int i) {
        return buf.capacity(i);
    }

    @Override
    public int maxCapacity() {
        return buf.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return buf.alloc();
    }

    @Override
    public ByteOrder order() {
        return buf.order();
    }

    @Override
    public ByteBuf order(ByteOrder byteOrder) {
        return buf.order(byteOrder);
    }

    @Override
    public ByteBuf unwrap() {
        return buf.unwrap();
    }

    @Override
    public boolean isDirect() {
        return buf.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return buf.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return buf.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int i) {
        return buf.readerIndex(i);
    }

    @Override
    public int writerIndex() {
        return buf.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int i) {
        return buf.writerIndex(i);
    }

    @Override
    public ByteBuf setIndex(int i, int j) {
        return buf.setIndex(i, j);
    }

    @Override
    public int readableBytes() {
        return buf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return buf.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return buf.isReadable();
    }

    @Override
    public boolean isReadable(int i) {
        return buf.isReadable(i);
    }

    @Override
    public boolean isWritable() {
        return buf.isWritable();
    }

    @Override
    public boolean isWritable(int i) {
        return buf.isWritable(i);
    }

    @Override
    public ByteBuf clear() {
        return buf.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return buf.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return buf.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return buf.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return buf.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return buf.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return buf.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int i) {
        return buf.ensureWritable(i);
    }

    @Override
    public int ensureWritable(int i, boolean bl) {
        return buf.ensureWritable(i, bl);
    }

    @Override
    public boolean getBoolean(int i) {
        return buf.getBoolean(i);
    }

    @Override
    public byte getByte(int i) {
        return buf.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return buf.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return buf.getShort(i);
    }

    @Override
    public short getShortLE(int i) {
        return buf.getShortLE(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return buf.getUnsignedShort(i);
    }

    @Override
    public int getUnsignedShortLE(int i) {
        return buf.getUnsignedShortLE(i);
    }

    @Override
    public int getMedium(int i) {
        return buf.getMedium(i);
    }

    @Override
    public int getMediumLE(int i) {
        return buf.getMediumLE(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return buf.getUnsignedMedium(i);
    }

    @Override
    public int getUnsignedMediumLE(int i) {
        return buf.getUnsignedMediumLE(i);
    }

    @Override
    public int getInt(int i) {
        return buf.getInt(i);
    }

    @Override
    public int getIntLE(int i) {
        return buf.getIntLE(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return buf.getUnsignedInt(i);
    }

    @Override
    public long getUnsignedIntLE(int i) {
        return buf.getUnsignedIntLE(i);
    }

    @Override
    public long getLong(int i) {
        return buf.getLong(i);
    }

    @Override
    public long getLongLE(int i) {
        return buf.getLongLE(i);
    }

    @Override
    public char getChar(int i) {
        return buf.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return buf.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return buf.getDouble(i);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf) {
        return buf.getBytes(i, byteBuf);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
        return buf.getBytes(i, byteBuf, j);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
        return buf.getBytes(i, byteBuf, j, k);
    }

    @Override
    public ByteBuf getBytes(int i, byte[] bs) {
        return buf.getBytes(i, bs);
    }

    @Override
    public ByteBuf getBytes(int i, byte[] bs, int j, int k) {
        return buf.getBytes(i, bs, j, k);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        return buf.getBytes(i, byteBuffer);
    }

    @Override
    public ByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
        return buf.getBytes(i, outputStream, j);
    }

    @Override
    public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int j) throws IOException {
        return buf.getBytes(i, gatheringByteChannel, j);
    }

    @Override
    public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return buf.getBytes(i, fileChannel, l, j);
    }

    @Override
    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return buf.getCharSequence(i, j, charset);
    }

    @Override
    public ByteBuf setBoolean(int i, boolean bl) {
        return buf.setBoolean(i, bl);
    }

    @Override
    public ByteBuf setByte(int i, int j) {
        return buf.setByte(i, j);
    }

    @Override
    public ByteBuf setShort(int i, int j) {
        return buf.setShort(i, j);
    }

    @Override
    public ByteBuf setShortLE(int i, int j) {
        return buf.setShortLE(i, j);
    }

    @Override
    public ByteBuf setMedium(int i, int j) {
        return buf.setMedium(i, j);
    }

    @Override
    public ByteBuf setMediumLE(int i, int j) {
        return buf.setMediumLE(i, j);
    }

    @Override
    public ByteBuf setInt(int i, int j) {
        return buf.setInt(i, j);
    }

    @Override
    public ByteBuf setIntLE(int i, int j) {
        return buf.setIntLE(i, j);
    }

    @Override
    public ByteBuf setLong(int i, long l) {
        return buf.setLong(i, l);
    }

    @Override
    public ByteBuf setLongLE(int i, long l) {
        return buf.setLongLE(i, l);
    }

    @Override
    public ByteBuf setChar(int i, int j) {
        return buf.setChar(i, j);
    }

    @Override
    public ByteBuf setFloat(int i, float f) {
        return buf.setFloat(i, f);
    }

    @Override
    public ByteBuf setDouble(int i, double d) {
        return buf.setDouble(i, d);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf) {
        return buf.setBytes(i, byteBuf);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        return buf.setBytes(i, byteBuf, j);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
        return buf.setBytes(i, byteBuf, j, k);
    }

    @Override
    public ByteBuf setBytes(int i, byte[] bs) {
        return buf.setBytes(i, bs);
    }

    @Override
    public ByteBuf setBytes(int i, byte[] bs, int j, int k) {
        return buf.setBytes(i, bs, j, k);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        return buf.setBytes(i, byteBuffer);
    }

    @Override
    public int setBytes(int i, InputStream inputStream, int j) throws IOException {
        return buf.setBytes(i, inputStream, j);
    }

    @Override
    public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int j) throws IOException {
        return buf.setBytes(i, scatteringByteChannel, j);
    }

    @Override
    public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return buf.setBytes(i, fileChannel, l, j);
    }

    @Override
    public ByteBuf setZero(int i, int j) {
        return buf.setZero(i, j);
    }

    @Override
    public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
        return buf.setCharSequence(i, charSequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public short readShortLE() {
        return buf.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return buf.readMedium();
    }

    @Override
    public int readMediumLE() {
        return buf.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public int readIntLE() {
        return buf.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public long readLongLE() {
        return buf.readLongLE();
    }

    @Override
    public char readChar() {
        return buf.readChar();
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public ByteBuf readBytes(int i) {
        return buf.readBytes(i);
    }

    @Override
    public ByteBuf readSlice(int i) {
        return buf.readSlice(i);
    }

    @Override
    public ByteBuf readRetainedSlice(int i) {
        return buf.readRetainedSlice(i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf) {
        return buf.readBytes(byteBuf);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf, int i) {
        return buf.readBytes(byteBuf, i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
        return buf.readBytes(byteBuf, i, j);
    }

    @Override
    public ByteBuf readBytes(byte[] bs) {
        return buf.readBytes(bs);
    }

    @Override
    public ByteBuf readBytes(byte[] bs, int i, int j) {
        return buf.readBytes(bs, i, j);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer byteBuffer) {
        return buf.readBytes(byteBuffer);
    }

    @Override
    public ByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        return buf.readBytes(outputStream, i);
    }

    @Override
    public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return buf.readBytes(gatheringByteChannel, i);
    }

    @Override
    public CharSequence readCharSequence(int i, Charset charset) {
        return buf.readCharSequence(i, charset);
    }

    @Override
    public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return buf.readBytes(fileChannel, l, i);
    }

    @Override
    public ByteBuf skipBytes(int i) {
        return buf.skipBytes(i);
    }

    @Override
    public ByteBuf writeBoolean(boolean bl) {
        return buf.writeBoolean(bl);
    }

    @Override
    public ByteBuf writeByte(int i) {
        return buf.writeByte(i);
    }

    @Override
    public ByteBuf writeShort(int i) {
        return buf.writeShort(i);
    }

    @Override
    public ByteBuf writeShortLE(int i) {
        return buf.writeShortLE(i);
    }

    @Override
    public ByteBuf writeMedium(int i) {
        return buf.writeMedium(i);
    }

    @Override
    public ByteBuf writeMediumLE(int i) {
        return buf.writeMediumLE(i);
    }

    @Override
    public ByteBuf writeInt(int i) {
        return buf.writeInt(i);
    }

    @Override
    public ByteBuf writeIntLE(int i) {
        return buf.writeIntLE(i);
    }

    @Override
    public ByteBuf writeLong(long l) {
        return buf.writeLong(l);
    }

    @Override
    public ByteBuf writeLongLE(long l) {
        return buf.writeLongLE(l);
    }

    @Override
    public ByteBuf writeChar(int i) {
        return buf.writeChar(i);
    }

    @Override
    public ByteBuf writeFloat(float f) {
        return buf.writeFloat(f);
    }

    @Override
    public ByteBuf writeDouble(double d) {
        return buf.writeDouble(d);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf) {
        return buf.writeBytes(byteBuf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf, int i) {
        return buf.writeBytes(byteBuf, i);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
        return buf.writeBytes(byteBuf, i, j);
    }

    @Override
    public ByteBuf writeBytes(byte[] bs) {
        return buf.writeBytes(bs);
    }

    @Override
    public ByteBuf writeBytes(byte[] bs, int i, int j) {
        return buf.writeBytes(bs, i, j);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return buf.writeBytes(byteBuffer);
    }

    @Override
    public int writeBytes(InputStream inputStream, int i) throws IOException {
        return buf.writeBytes(inputStream, i);
    }

    @Override
    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return buf.writeBytes(scatteringByteChannel, i);
    }

    @Override
    public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return buf.writeBytes(fileChannel, l, i);
    }

    @Override
    public ByteBuf writeZero(int i) {
        return buf.writeZero(i);
    }

    @Override
    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return buf.writeCharSequence(charSequence, charset);
    }

    @Override
    public int indexOf(int i, int j, byte b) {
        return buf.indexOf(i, j, b);
    }

    @Override
    public int bytesBefore(byte b) {
        return buf.bytesBefore(b);
    }

    @Override
    public int bytesBefore(int i, byte b) {
        return buf.bytesBefore(i, b);
    }

    @Override
    public int bytesBefore(int i, int j, byte b) {
        return buf.bytesBefore(i, j, b);
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return buf.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
        return buf.forEachByte(i, j, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return buf.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
        return buf.forEachByteDesc(i, j, byteProcessor);
    }

    @Override
    public ByteBuf copy() {
        return buf.copy();
    }

    @Override
    public ByteBuf copy(int i, int j) {
        return buf.copy(i, j);
    }

    @Override
    public ByteBuf slice() {
        return buf.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return buf.retainedSlice();
    }

    @Override
    public ByteBuf slice(int i, int j) {
        return buf.slice(i, j);
    }

    @Override
    public ByteBuf retainedSlice(int i, int j) {
        return buf.retainedSlice(i, j);
    }

    @Override
    public ByteBuf duplicate() {
        return buf.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return buf.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int i, int j) {
        return buf.nioBuffer(i, j);
    }

    @Override
    public ByteBuffer internalNioBuffer(int i, int j) {
        return buf.internalNioBuffer(i, j);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return buf.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int i, int j) {
        return buf.nioBuffers(i, j);
    }

    @Override
    public boolean hasArray() {
        return buf.hasArray();
    }

    @Override
    public byte[] array() {
        return buf.array();
    }

    @Override
    public int arrayOffset() {
        return buf.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return buf.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return buf.toString(charset);
    }

    @Override
    public String toString(int i, int j, Charset charset) {
        return buf.toString(i, j, charset);
    }

    @Override
    public int hashCode() {
        return buf.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return buf.equals(object);
    }

    @Override
    public int compareTo(ByteBuf byteBuf) {
        return buf.compareTo(byteBuf);
    }

    @Override
    public String toString() {
        return buf.toString();
    }

    @Override
    public ByteBuf retain(int i) {
        return buf.retain(i);
    }

    @Override
    public ByteBuf retain() {
        return buf.retain();
    }

    @Override
    public ByteBuf touch() {
        return buf.touch();
    }

    @Override
    public ByteBuf touch(Object object) {
        return buf.touch(object);
    }

    @Override
    public int refCnt() {
        return buf.refCnt();
    }

    @Override
    public boolean release() {
        return buf.release();
    }

    @Override
    public boolean release(int i) {
        return buf.release(i);
    }
}

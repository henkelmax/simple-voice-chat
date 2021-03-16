package de.maxhenkel.voicechat.voice.common;

import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.maxhenkel.opus4j.Opus;

public class OpusEncoder {

    protected PointerByReference opusEncoder;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;

    public OpusEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        IntBuffer error = IntBuffer.allocate(1);
        opusEncoder = Opus.INSTANCE.opus_encoder_create(sampleRate, 1, application, error);
        if (error.get() != Opus.OPUS_OK && opusEncoder == null) {
            throw new IllegalStateException("Opus encoder error " + error.get());
        }
    }

    public byte[] encode(byte[] rawAudio) {
        ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(rawAudio.length / 2);
        ByteBuffer encoded = ByteBuffer.allocate(maxPayloadSize);
        for (int i = 0; i < rawAudio.length; i += 2) {
            short toShort = Utils.bytesToShort(rawAudio[i], rawAudio[i + 1]);

            nonEncodedBuffer.put(toShort);
        }
        nonEncodedBuffer.flip();

        int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, frameSize / 2, encoded, encoded.capacity());

        if (result < 0) {
            throw new RuntimeException("Failed to encode audio data");
        }

        byte[] audio = new byte[result];
        encoded.get(audio);
        return audio;
    }

    public void close() {
        Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
    }

}

package de.maxhenkel.voicechat.voice.client;

import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.maxhenkel.opus4j.Opus;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;

public class OpusEncoder {

    protected PointerByReference opusEncoder;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;
    protected int application;

    private OpusEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.application = application;
        open();
    }

    public void open() {
        if (opusEncoder != null) {
            return;
        }
        IntBuffer error = IntBuffer.allocate(1);
        opusEncoder = Opus.INSTANCE.opus_encoder_create(sampleRate, 1, application, error);
        if (error.get() != Opus.OPUS_OK && opusEncoder == null) {
            throw new IllegalStateException("Opus encoder error " + error.get());
        }
    }

    public byte[] encode(short[] rawAudio) {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        ShortBuffer nonEncodedBuffer = ShortBuffer.wrap(rawAudio);
        ByteBuffer encoded = ByteBuffer.allocate(maxPayloadSize);

        int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, frameSize, encoded, encoded.capacity());

        if (result < 0) {
            throw new RuntimeException("Failed to encode audio data");
        }

        byte[] audio = new byte[result];
        encoded.get(audio);
        return audio;
    }

    public void resetState() {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        Opus.INSTANCE.opus_encoder_ctl(opusEncoder, Opus.INSTANCE.OPUS_RESET_STATE);
    }

    public boolean isClosed() {
        return opusEncoder == null;
    }

    public void close() {
        if (isClosed()) {
            return;
        }
        Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
        opusEncoder = null;
    }

    @Nullable
    public static OpusEncoder createEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        return Utils.createSafe(() -> new OpusEncoder(sampleRate, frameSize, maxPayloadSize, application));
    }

}

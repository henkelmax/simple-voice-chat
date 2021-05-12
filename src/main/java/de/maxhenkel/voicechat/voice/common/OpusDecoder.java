package de.maxhenkel.voicechat.voice.common;

import com.sun.jna.ptr.PointerByReference;
import de.maxhenkel.opus4j.Opus;
import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OpusDecoder {

    protected PointerByReference opusDecoder;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;
    protected boolean closed;

    public OpusDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        IntBuffer error = IntBuffer.allocate(1);
        opusDecoder = Opus.INSTANCE.opus_decoder_create(sampleRate, 1, error);
        if (error.get() != Opus.OPUS_OK && opusDecoder == null) {
            throw new IllegalStateException("Opus decoder error " + error.get());
        }
        Voicechat.LOGGER.info("Initializing Opus decoder with sample rate " + sampleRate + " Hz, frame size " + frameSize + " bytes and max payload size " + maxPayloadSize + " bytes");
    }

    public byte[] decode(@Nullable byte[] data) {
        if (closed) {
            throw new IllegalStateException("Trying to decode with a closed decoder");
        }
        int result;
        ShortBuffer decoded = ShortBuffer.allocate(4096);
        if (data == null || data.length == 0) {
            result = Opus.INSTANCE.opus_decode(opusDecoder, null, 0, decoded, frameSize / 2, 0);
        } else {
            result = Opus.INSTANCE.opus_decode(opusDecoder, data, data.length, decoded, frameSize / 2, 0);
        }

        if (result < 0) {
            throw new RuntimeException("Failed to decode audio data");
        }

        short[] audio = new short[result];
        decoded.get(audio);

        byte[] outData = new byte[audio.length * 2];

        for (int i = 0; i < audio.length; i++) {
            byte[] split = Utils.shortToBytes(audio[i]);
            outData[i * 2] = split[0];
            outData[i * 2 + 1] = split[1];
        }
        return outData;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
    }

}

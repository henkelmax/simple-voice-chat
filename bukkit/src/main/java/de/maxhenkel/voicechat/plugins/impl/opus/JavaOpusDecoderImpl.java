package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.voicechat.Voicechat;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import javax.annotation.Nullable;

public class JavaOpusDecoderImpl implements de.maxhenkel.voicechat.api.opus.OpusDecoder {

    protected OpusDecoder opusDecoder;
    protected short[] buffer;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;

    public JavaOpusDecoderImpl(int sampleRate, int frameSize, int maxPayloadSize) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.buffer = new short[4096];
        open();
    }

    private void open() {
        if (opusDecoder != null) {
            return;
        }
        try {
            opusDecoder = new OpusDecoder(sampleRate, 1);
        } catch (OpusException e) {
            throw new IllegalStateException("Opus decoder error " + e.getMessage());
        }
        Voicechat.LOGGER.info("Initializing Opus decoder with sample rate " + sampleRate + " Hz, frame size " + frameSize + " bytes and max payload size " + maxPayloadSize + " bytes");
    }

    @Override
    public short[] decode(@Nullable byte[] data) {
        if (isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        int result;

        try {
            if (data == null || data.length == 0) {
                result = opusDecoder.decode(null, 0, 0, buffer, 0, frameSize, false);
            } else {
                result = opusDecoder.decode(data, 0, data.length, buffer, 0, frameSize, false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode audio data: " + e.getMessage());
        }

        short[] audio = new short[result];
        System.arraycopy(buffer, 0, audio, 0, result);
        return audio;
    }

    @Override
    public boolean isClosed() {
        return opusDecoder == null;
    }

    @Override
    public void close() {
        if (opusDecoder == null) {
            return;
        }
        opusDecoder = null;
    }

    @Override
    public void resetState() {
        if (isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        opusDecoder.resetState();
    }

}

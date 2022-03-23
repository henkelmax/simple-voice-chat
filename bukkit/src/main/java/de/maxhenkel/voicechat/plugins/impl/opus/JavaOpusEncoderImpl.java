package de.maxhenkel.voicechat.plugins.impl.opus;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;

public class JavaOpusEncoderImpl implements de.maxhenkel.voicechat.api.opus.OpusEncoder {

    protected OpusEncoder opusEncoder;
    protected byte[] buffer;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;
    protected OpusApplication application;

    public JavaOpusEncoderImpl(int sampleRate, int frameSize, int maxPayloadSize, OpusApplication application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.application = application;
        this.buffer = new byte[maxPayloadSize];
        open();
    }

    private void open() {
        if (opusEncoder != null) {
            return;
        }
        try {
            opusEncoder = new OpusEncoder(sampleRate, 1, application);
        } catch (Exception e) {
            throw new IllegalStateException("Opus encoder error " + e.getMessage());
        }
    }

    @Override
    public byte[] encode(short[] rawAudio) {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }

        int result;
        try {
            result = opusEncoder.encode(rawAudio, 0, frameSize, buffer, 0, buffer.length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode audio data: " + e.getMessage());
        }

        if (result < 0) {
            throw new RuntimeException("Failed to encode audio data");
        }

        byte[] audio = new byte[result];
        System.arraycopy(buffer, 0, audio, 0, result);
        return audio;
    }

    @Override
    public void resetState() {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        opusEncoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return opusEncoder == null;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        opusEncoder = null;
    }
}

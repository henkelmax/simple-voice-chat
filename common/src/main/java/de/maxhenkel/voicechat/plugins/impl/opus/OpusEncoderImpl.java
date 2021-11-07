package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.annotation.Nullable;

public class OpusEncoderImpl implements OpusEncoder {

    private final de.maxhenkel.voicechat.voice.client.OpusEncoder encoder;

    public OpusEncoderImpl(de.maxhenkel.voicechat.voice.client.OpusEncoder encoder) {
        this.encoder = encoder;
    }

    @Nullable
    public static OpusEncoderImpl create() {
        de.maxhenkel.voicechat.voice.client.OpusEncoder encoder = de.maxhenkel.voicechat.voice.client.OpusEncoder.createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024, ServerConfig.Codec.VOIP.getOpusValue());
        if (encoder == null) {
            return null;
        }

        return new OpusEncoderImpl(encoder);
    }

    @Override
    public byte[] encode(short[] rawAudio) {
        return encoder.encode(rawAudio);
    }

    @Override
    public void resetState() {
        encoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return encoder.isClosed();
    }

    @Override
    public void close() {
        encoder.close();
    }
}

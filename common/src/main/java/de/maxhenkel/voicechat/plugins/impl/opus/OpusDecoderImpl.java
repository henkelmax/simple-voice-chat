package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.annotation.Nullable;

public class OpusDecoderImpl implements OpusDecoder {

    private final de.maxhenkel.voicechat.voice.client.OpusDecoder decoder;

    public OpusDecoderImpl(de.maxhenkel.voicechat.voice.client.OpusDecoder decoder) {
        this.decoder = decoder;
    }

    @Nullable
    public static OpusDecoderImpl create() {
        de.maxhenkel.voicechat.voice.client.OpusDecoder encoder = de.maxhenkel.voicechat.voice.client.OpusDecoder.createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024);
        if (encoder == null) {
            return null;
        }

        return new OpusDecoderImpl(encoder);
    }


    @Override
    public short[] decode(@Nullable byte[] data) {
        return decoder.decode(data);
    }

    @Override
    public void resetState() {
        decoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return decoder.isClosed();
    }

    @Override
    public void close() {
        decoder.close();
    }
}

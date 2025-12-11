package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.ShortArrayBuffer;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.maxhenkel.voicechat.natives.LameManager;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

public class Mp3DecoderImpl implements Mp3Decoder {

    private final de.maxhenkel.lame4j.Mp3Decoder decoder;
    private IOException decodeError;
    @Nullable
    private short[] samples;
    @Nullable
    private AudioFormat audioFormat;
    private int bitrate;

    private Mp3DecoderImpl(de.maxhenkel.lame4j.Mp3Decoder decoder) {
        this.decoder = decoder;
        this.bitrate = -1;
    }

    private void decodeIfNecessary() throws IOException {
        if (decodeError != null) {
            throw decodeError;
        }
        try {
            if (samples == null) {
                ShortArrayBuffer sampleBuffer = new ShortArrayBuffer(2048);
                while (true) {
                    short[] samples = decoder.decodeNextFrame();
                    if (samples == null) {
                        break;
                    }
                    sampleBuffer.writeShorts(samples);
                }
                samples = sampleBuffer.toShortArray();
                audioFormat = decoder.createAudioFormat();
                bitrate = decoder.getBitRate();
            }
        } catch (IOException e) {
            decodeError = e;
            throw e;
        } finally {
            decoder.close();
        }
    }

    @Override
    public short[] decode() throws IOException {
        decodeIfNecessary();
        return samples;
    }

    @Override
    public AudioFormat getAudioFormat() throws IOException {
        decodeIfNecessary();
        return audioFormat;
    }

    @Override
    public int getBitrate() throws IOException {
        decodeIfNecessary();
        return bitrate;
    }

    @Nullable
    public static Mp3Decoder createDecoder(InputStream inputStream) {
        de.maxhenkel.lame4j.Mp3Decoder dec = LameManager.createDecoder(inputStream);
        if (dec == null) {
            return null;
        }
        return new Mp3DecoderImpl(dec);
    }

}

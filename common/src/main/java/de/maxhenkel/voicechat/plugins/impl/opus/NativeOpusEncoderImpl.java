package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.UnknownPlatformException;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import java.io.IOException;

public class NativeOpusEncoderImpl extends de.maxhenkel.opus4j.OpusEncoder implements OpusEncoder {

    public NativeOpusEncoderImpl(int sampleRate, int channels, Application application) throws IOException, UnknownPlatformException {
        super(sampleRate, channels, application);
    }

}

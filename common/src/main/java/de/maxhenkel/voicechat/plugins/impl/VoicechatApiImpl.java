package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusDecoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusEncoderImpl;

import javax.annotation.Nullable;

public class VoicechatApiImpl implements VoicechatApi {

    @Nullable
    @Override
    public OpusEncoder createEncoder() {
        return OpusEncoderImpl.create();
    }

    @Nullable
    @Override
    public OpusDecoder createDecoder() {
        return OpusDecoderImpl.create();
    }

}

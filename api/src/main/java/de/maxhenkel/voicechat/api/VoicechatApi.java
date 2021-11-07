package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import javax.annotation.Nullable;

public interface VoicechatApi {

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it
     *
     * @return the opus encoder or <code>null</code> if there are no natives for this platform.
     */
    @Nullable
    OpusEncoder createEncoder();

    /**
     * Creates a new opus decoder.
     * Note that the decoder needs to be closed after you are finished using it
     *
     * @return the opus decoder or <code>null</code> if there are no natives for this platform.
     */
    @Nullable
    OpusDecoder createDecoder();

}

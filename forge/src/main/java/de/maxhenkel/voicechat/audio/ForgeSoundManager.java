package de.maxhenkel.voicechat.audio;

import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.SpeakerException;

import javax.annotation.Nullable;

public class ForgeSoundManager extends SoundManager {

    public ForgeSoundManager(@Nullable String deviceName) throws SpeakerException {
        super(deviceName);
    }
}

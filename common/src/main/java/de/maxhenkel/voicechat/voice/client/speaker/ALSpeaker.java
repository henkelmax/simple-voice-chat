package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.voice.client.SoundManager;
import org.lwjgl.openal.AL10;

import javax.annotation.Nullable;
import java.util.UUID;

@Deprecated
public class ALSpeaker extends ALSpeakerBase {

    public ALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, bufferSize, audioChannelId);
    }

    @Override
    protected int getFormat() {
        return AL10.AL_FORMAT_MONO16;
    }

}

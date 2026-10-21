package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.voice.client.SoundManager;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class ALSpeaker extends ALSpeakerBase {

    public ALSpeaker(SoundManager soundManager, int sampleRate, int frameSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, frameSize, audioChannelId);
    }

    @Override
    protected int getFormat() {
        return AL11.AL_FORMAT_MONO16;
    }

}

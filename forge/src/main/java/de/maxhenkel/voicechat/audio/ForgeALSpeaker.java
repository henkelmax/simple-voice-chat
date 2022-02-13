package de.maxhenkel.voicechat.audio;

import de.maxhenkel.voicechat.voice.client.ALSpeaker;
import de.maxhenkel.voicechat.voice.client.SoundManager;

public class ForgeALSpeaker extends ALSpeaker {

    public ForgeALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize) {
        super(soundManager, sampleRate, bufferSize);
    }
}

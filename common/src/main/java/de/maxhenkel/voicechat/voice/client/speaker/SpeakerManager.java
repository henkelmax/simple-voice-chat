package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static ALSpeakerBase createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        ALSpeakerBase speaker;
        switch (VoicechatClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL:
            default:
                speaker = new ALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
                break;
            case REDUCED:
                speaker = new FakeALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
                break;
            case OFF:
                speaker = new MonoALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
                break;
        }
        speaker.open();
        return speaker;
    }

}

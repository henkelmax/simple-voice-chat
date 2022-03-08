package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static ALSpeakerBase createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        ALSpeakerBase speaker = switch (VoicechatClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL -> new ALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
            case REDUCED -> new FakeALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
            case OFF -> new MonoALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
        };
        speaker.open();
        return speaker;
    }

}

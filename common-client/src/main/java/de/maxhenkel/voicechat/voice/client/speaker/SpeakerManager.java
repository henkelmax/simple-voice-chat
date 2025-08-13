package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static Speaker createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        ALSpeakerBase speaker;
        switch (VoicechatClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL:
            default:
                speaker = new ALSpeaker(soundManager, AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, audioChannel);
                break;
            case REDUCED:
                speaker = new FakeALSpeaker(soundManager, AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, audioChannel);
                break;
            case OFF:
                speaker = new MonoALSpeaker(soundManager, AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, audioChannel);
                break;
        }
        speaker.open();
        return speaker;
    }

}

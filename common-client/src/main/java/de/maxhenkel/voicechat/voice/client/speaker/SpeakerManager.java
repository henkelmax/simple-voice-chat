package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static Speaker createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        ALSpeakerBase speaker = switch (VoicechatClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL -> new ALSpeaker(soundManager, Utils.SAMPLE_RATE, Utils.FRAME_SIZE, audioChannel);
            case REDUCED -> new FakeALSpeaker(soundManager, Utils.SAMPLE_RATE, Utils.FRAME_SIZE, audioChannel);
            case OFF -> new MonoALSpeaker(soundManager, Utils.SAMPLE_RATE, Utils.FRAME_SIZE, audioChannel);
        };
        speaker.open();
        return speaker;
    }

}

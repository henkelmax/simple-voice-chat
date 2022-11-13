package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static Speaker createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        Speaker speaker;

        switch (VoicechatClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL:
            case REDUCED:
            default:
                speaker = new JavaSpeaker();
                break;
            case OFF:
                // TODO Add mono support
                speaker = new JavaSpeaker();
                break;
        }

        speaker.open();
        return speaker;
    }

}

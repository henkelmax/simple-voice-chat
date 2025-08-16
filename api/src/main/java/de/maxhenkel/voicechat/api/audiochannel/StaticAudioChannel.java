package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.VoicechatConnection;

public interface StaticAudioChannel extends AudioChannel {

    void addTarget(VoicechatConnection target);

    void removeTarget(VoicechatConnection target);

    void clearTargets();

}

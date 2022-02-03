package de.maxhenkel.voicechat.api;

public interface VoicechatClientApi extends VoicechatApi {

    boolean isMuted();

    boolean isDisabled();

    boolean isDisconnected();

}

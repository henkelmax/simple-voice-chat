package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;

public interface ClientEvent extends Event {

    /**
     * @return the voice chat client API
     */
    VoicechatClientApi getVoicechat();

}

package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;

public class Utils {

    public static final int MAX_VOICE_CHAT_PACKET_SIZE = 2048;

    /**
     * Gets the default voice chat distance
     *
     * @return the default voice chat distance
     */
    public static float getDefaultDistance() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

}

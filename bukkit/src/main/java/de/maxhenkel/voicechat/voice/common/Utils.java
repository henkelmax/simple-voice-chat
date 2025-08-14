package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;

public class Utils {

    /**
     * Gets the default voice chat distance
     *
     * @return the default voice chat distance
     */
    public static float getDefaultDistance() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

}

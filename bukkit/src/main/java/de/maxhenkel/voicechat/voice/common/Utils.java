package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;

import java.security.SecureRandom;
import java.util.UUID;

public class Utils {

    public static final int MAX_VOICE_CHAT_PACKET_SIZE = 2048;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Unlike {@link UUID#randomUUID()}, all 128 bits are random.
     *
     * @return a UUID generated with a {@link SecureRandom}
     */
    public static UUID generateSecureRandomUUID() {
        return new UUID(SECURE_RANDOM.nextLong(), SECURE_RANDOM.nextLong());
    }

    /**
     * @param length the number of bytes
     * @return bytes generated with a {@link SecureRandom}
     */
    public static byte[] generateSecureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Gets the default voice chat distance
     *
     * @return the default voice chat distance
     */
    public static float getDefaultDistance() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

}

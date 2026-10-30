package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

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

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static float normalizeAngle(float angle) {
        angle = angle % 360F;
        if (angle <= -180F) {
            angle += 360F;
        } else if (angle > 180F) {
            angle -= 360F;
        }
        return angle;
    }

    public static float angle(Vec2f vec1, Vec2f vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }

    private static double magnitude(Vec2f vec1) {
        return Math.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2));
    }

    private static float multiply(Vec2f vec1, Vec2f vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    private static Vec2f rotate(Vec2f vec, float angle) {
        return new Vec2f(vec.x * MathHelper.cos(angle) - vec.y * MathHelper.sin(angle), vec.x * MathHelper.sin(angle) + vec.y * MathHelper.cos(angle));
    }

    public static float getDefaultDistanceServer() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

}

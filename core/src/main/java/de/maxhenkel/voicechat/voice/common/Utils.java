package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Position;

public class Utils {

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

    public static float angle(float vec1x, float vec2x, float vec1y, float vec2y) {
        return (float) Math.toDegrees(Math.atan2(vec1x * vec2x + vec1y * vec2y, vec1x * vec2y - vec1y * vec2x));
    }

    public static float getDefaultDistanceServer() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

    public static double distanceToSqr(Position vec3, Position vec32) {
        double d = vec32.getX() - vec3.getX();
        double e = vec32.getY() - vec3.getY();
        double f = vec32.getZ() - vec3.getZ();
        return d * d + e * e + f * f;
    }
}

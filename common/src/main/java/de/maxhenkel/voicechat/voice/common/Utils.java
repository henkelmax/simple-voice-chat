package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

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

    public static float angle(Vec2 vec1, Vec2 vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }

    private static double magnitude(Vec2 vec1) {
        return Math.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2));
    }

    private static float multiply(Vec2 vec1, Vec2 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    private static Vec2 rotate(Vec2 vec, float angle) {
        return new Vec2(vec.x * Mth.cos(angle) - vec.y * Mth.sin(angle), vec.x * Mth.sin(angle) + vec.y * Mth.cos(angle));
    }

    public static float getDefaultDistanceServer() {
        return Voicechat.SERVER_CONFIG.voiceChatDistance.get().floatValue();
    }

}

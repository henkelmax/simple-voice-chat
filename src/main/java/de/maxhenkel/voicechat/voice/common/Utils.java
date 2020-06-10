package de.maxhenkel.voicechat.voice.common;

public class Utils {
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }
}

package de.maxhenkel.voicechat;

public class BuildConstants {

    public static final int COMPATIBILITY_VERSION;

    static {
        String compatibilityVersionString = "${compatibility_version}";
        int compatibilityVersion;
        try {
            compatibilityVersion = Integer.parseInt(compatibilityVersionString);
        } catch (NumberFormatException e1) {
            try {
                compatibilityVersion = Integer.parseInt(System.getenv("COMPATIBILITY_VERSION"));
            } catch (NumberFormatException e2) {
                compatibilityVersion = -1;
            }
        }
        COMPATIBILITY_VERSION = compatibilityVersion;
    }

}

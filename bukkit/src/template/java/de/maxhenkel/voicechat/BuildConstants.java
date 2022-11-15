package de.maxhenkel.voicechat;

public class BuildConstants {

    public static final int COMPATIBILITY_VERSION;
    public static final String TARGET_BUKKIT_VERSION = "${target_bukkit_version}";

    static {
        String compatibilityVersionString = "${compatibility_version}";
        int compatibilityVersion;
        try {
            compatibilityVersion = Integer.parseInt(compatibilityVersionString);
        } catch (NumberFormatException e) {
            compatibilityVersion = -1;
        }
        COMPATIBILITY_VERSION = compatibilityVersion;
    }

}

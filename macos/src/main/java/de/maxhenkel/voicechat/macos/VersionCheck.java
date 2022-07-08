package de.maxhenkel.voicechat.macos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionCheck {

    public static Pattern VERSIONING_PATTERN = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)){0,1}){0,1}$");

    public static boolean isMinimumVersion(int major, int minor, int patch) {
        String version = System.getProperty("os.version");
        if (version == null) {
            return true;
        }
        Matcher matcher = VERSIONING_PATTERN.matcher(version);
        if (!matcher.matches()) {
            return true;
        }
        String majorGroup = matcher.group("major");
        String minorGroup = matcher.group("minor");
        String patchGroup = matcher.group("patch");
        int actualMajor = majorGroup == null ? 0 : Integer.parseInt(majorGroup);
        int actualMinor = minorGroup == null ? 0 : Integer.parseInt(minorGroup);
        int actualPatch = patchGroup == null ? 0 : Integer.parseInt(patchGroup);
        if (major > actualMajor) {
            return false;
        } else if (major == actualMajor) {
            if (minor > actualMinor) {
                return false;
            } else if (minor == actualMinor) {
                return patch <= actualPatch;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isCompatible() {
        return isMinimumVersion(10, 15, 0);
    }

}

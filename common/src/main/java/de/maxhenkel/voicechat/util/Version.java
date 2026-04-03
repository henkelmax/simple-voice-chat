package de.maxhenkel.voicechat.util;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    public static Pattern ALSOFT_PATTERN = Pattern.compile("^.* ALSOFT (?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?)?$");
    public static Pattern PATTERN = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?)?$");

    public final int major;
    public final int minor;
    public final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Nullable
    public static Version fromOpenALVersion(String version) {
        return fromRegex(ALSOFT_PATTERN, version);
    }

    @Nullable
    public static Version fromVersionString(String version) {
        return fromRegex(PATTERN, version);
    }

    @Nullable
    private static Version fromRegex(Pattern pattern, String version) {
        Matcher matcher = pattern.matcher(version);
        if (!matcher.matches()) {
            return null;
        }
        String majorGroup = matcher.group("major");
        String minorGroup = matcher.group("minor");
        String patchGroup = matcher.group("patch");
        int actualMajor = majorGroup == null ? 0 : Integer.parseInt(majorGroup);
        int actualMinor = minorGroup == null ? 0 : Integer.parseInt(minorGroup);
        int actualPatch = patchGroup == null ? 0 : Integer.parseInt(patchGroup);
        return new Version(actualMajor, actualMinor, actualPatch);
    }

    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public int compareTo(Version o) {
        int majorDiff = Integer.compare(major, o.major);
        if (majorDiff != 0) {
            return majorDiff;
        }

        int minorDiff = Integer.compare(minor, o.minor);
        if (minorDiff != 0) {
            return minorDiff;
        }

        return Integer.compare(patch, o.patch);
    }
}

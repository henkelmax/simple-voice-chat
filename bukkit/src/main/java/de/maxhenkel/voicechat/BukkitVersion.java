package de.maxhenkel.voicechat;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitVersion extends Version {

    public static final Pattern BUKKIT_VERSION_REGEX = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?-R(?<revision>[\\d.-]+)(?:-SNAPSHOT)?$");

    protected final String revision;

    public BukkitVersion(int major, int minor, int patch, String revision) {
        super(major, minor, patch);
        this.revision = revision;
    }

    private static BukkitVersion fromRegex(Matcher matcher) {
        String major = matcher.group("major");
        String minor = matcher.group("minor");
        String patch = matcher.group("patch");
        String revision = matcher.group("revision");
        if (patch == null || patch.isEmpty()) {
            patch = "0";
        }
        return new BukkitVersion(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), revision);
    }

    @Nullable
    public static BukkitVersion parseBukkitVersion(String bukkitVersion) {
        Matcher targetMatcher = BUKKIT_VERSION_REGEX.matcher(bukkitVersion);

        if (!targetMatcher.matches()) {
            return null;
        }

        return BukkitVersion.fromRegex(targetMatcher);
    }

    @Nullable
    public static BukkitVersion getTargetVersion() {
        return parseBukkitVersion(BuildConstants.TARGET_BUKKIT_VERSION);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BukkitVersion)) {
            return false;
        }

        BukkitVersion that = (BukkitVersion) o;

        if (major != that.major) {
            return false;
        }
        if (minor != that.minor) {
            return false;
        }
        if (patch != that.patch) {
            return false;
        }
        return revision.equals(that.revision);
    }

    @Override
    public String toString() {
        if (patch <= 0) {
            return major + "." + minor + "-R" + revision;
        }
        return major + "." + minor + "." + patch + "-R" + revision;
    }
}

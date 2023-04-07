package de.maxhenkel.voicechat;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitVersion {

    public static final Pattern BUKKIT_VERSION_REGEX = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?-R(?<revision>[\\d.-]+)(?:-SNAPSHOT)?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final String revision;

    public BukkitVersion(int major, int minor, int patch, String revision) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
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
    public static BukkitVersion getVersion() {
        return parseBukkitVersion(Bukkit.getBukkitVersion());
    }

    @Nullable
    public static BukkitVersion parseBukkitVersion(String bukkitVersion) {
        Matcher targetMatcher = BUKKIT_VERSION_REGEX.matcher(bukkitVersion);

        if (!targetMatcher.matches()) {
            Voicechat.LOGGER.fatal("Failed to parse target Bukkit version: {}", bukkitVersion);
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
        if (o == null || getClass() != o.getClass()) {
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
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (patch <= 0) {
            return major + "." + minor + "-R" + revision;
        }
        return major + "." + minor + "." + patch + "-R" + revision;
    }
}

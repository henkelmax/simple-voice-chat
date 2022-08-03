package de.maxhenkel.voicechat;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitVersionCheck {

    public static final Pattern BUKKIT_PACKAGE_REGEX = Pattern.compile("^org\\.bukkit\\.craftbukkit\\.v(?<major>\\d+)_(?<minor>\\d+)(?:_?(?<patch>\\d+))?_R(?<revision>\\d+)$");
    public static final Pattern BUKKIT_VERSION_REGEX = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.?(?<patch>\\d+))?-R(?<revision>\\d+)$");

    public static boolean matchesTargetVersion() {
        BukkitVersion version = getVersion();
        BukkitVersion targetVersion = getTargetVersion();

        if (version == null || targetVersion == null) {
            return false;
        }

        if (version.equals(targetVersion)) {
            return true;
        }

        Voicechat.LOGGER.fatal("Incompatible Bukkit version '{}'! Simple Voice Chat needs '{}'", version, targetVersion);
        return false;
    }

    @Nullable
    public static BukkitVersion getTargetVersion() {
        String targetVersion;
        try {
            targetVersion = Voicechat.readMetaInf("Target-Bukkit-Version");
        } catch (IOException e) {
            Voicechat.LOGGER.fatal("Failed to read target Bukkit version", e);
            return null;
        }
        if (targetVersion == null) {
            Voicechat.LOGGER.fatal("Failed to read target Bukkit version");
            return null;
        }

        Matcher targetMatcher = BUKKIT_VERSION_REGEX.matcher(targetVersion);

        if (!targetMatcher.matches()) {
            Voicechat.LOGGER.fatal("Failed to parse target Bukkit version: {}", targetVersion);
            return null;
        }

        return BukkitVersion.fromRegex(targetMatcher);
    }

    @Nullable
    public static BukkitVersion getVersion() {
        String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

        Matcher matcher = BUKKIT_PACKAGE_REGEX.matcher(bukkitPackage);

        if (!matcher.matches()) {
            Voicechat.LOGGER.fatal("Failed to parse Bukkit version: {}", bukkitPackage);
            return null;
        }

        return BukkitVersion.fromRegex(matcher);
    }

    public static class BukkitVersion {
        private final int major;
        private final int minor;
        private final int patch;
        private final int revision;

        public BukkitVersion(int major, int minor, int patch, int revision) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.revision = revision;
        }

        public static BukkitVersion fromRegex(Matcher matcher) {
            String major = matcher.group("major");
            String minor = matcher.group("minor");
            String patch = matcher.group("patch");
            String revision = matcher.group("revision");
            if (patch == null || patch.isEmpty()) {
                patch = "0";
            }
            return new BukkitVersion(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), Integer.parseInt(revision));
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
            return revision == that.revision;
        }

        @Override
        public String toString() {
            if (patch <= 0) {
                return major + "." + minor + "-R" + revision;
            }
            return major + "." + minor + "." + patch + "-R" + revision;
        }
    }

}

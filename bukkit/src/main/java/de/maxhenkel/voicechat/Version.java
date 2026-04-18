package de.maxhenkel.voicechat;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class Version {

    protected final int major;
    protected final int minor;
    protected final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Nullable
    public static Version parse(String version) {
        Version v = BukkitVersion.parseBukkitVersion(version);
        if (v == null) {
            v = PaperVersion.parsePaperVersion(version);
        }
        return v;
    }

    @Nullable
    public static Version getVersion() {
        return parse(Bukkit.getBukkitVersion());
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version that = (Version) o;

        if (major != that.major) {
            return false;
        }
        if (minor != that.minor) {
            return false;
        }
        return patch == that.patch;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }
}

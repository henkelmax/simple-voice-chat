package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;

public class IncompatibleBukkitVersionException extends Exception {

    private BukkitVersion version;

    public IncompatibleBukkitVersionException(BukkitVersion version, String message) {
        super(message);
        this.version = version;
    }

    public IncompatibleBukkitVersionException(BukkitVersion version, String message, Throwable cause) {
        super(message, cause);
        this.version = version;
    }

    public IncompatibleBukkitVersionException(BukkitVersion version, Throwable cause) {
        super(cause);
        this.version = version;
    }

    public BukkitVersion getVersion() {
        return version;
    }
}

package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.compatibility.ReflectionUtils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperVersion extends Version {

    public static final Pattern PAPER_VERSION_REGEX = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?\\.build\\.(?<build>.*)$");

    private final String build;

    public PaperVersion(int major, int minor, int patch, String build) {
        super(major, minor, patch);
        this.build = build;
    }

    private static PaperVersion fromRegex(Matcher matcher) {
        String major = matcher.group("major");
        String minor = matcher.group("minor");
        String patch = matcher.group("patch");
        String build = matcher.group("build");
        if (patch == null || patch.isEmpty()) {
            patch = "0";
        }
        return new PaperVersion(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), build);
    }

    public static boolean isPaper() {
        return ReflectionUtils.doesClassExist("io.papermc.paper.ServerBuildInfo");
    }

    @Nullable
    public static PaperVersion parsePaperVersion(String version) {
        if (!isPaper()) {
            return null;
        }

        Matcher targetMatcher = PAPER_VERSION_REGEX.matcher(version);

        if (!targetMatcher.matches()) {
            return null;
        }

        return PaperVersion.fromRegex(targetMatcher);
    }

    @Override
    public String toString() {
        if (patch <= 0) {
            return major + "." + minor + ".build." + build;
        }
        return major + "." + minor + "." + patch + ".build." + build;
    }
}

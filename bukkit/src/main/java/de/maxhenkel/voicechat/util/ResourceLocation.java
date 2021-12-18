package de.maxhenkel.voicechat.util;

public class ResourceLocation {

    private final String namespace;
    private final String path;

    public ResourceLocation(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "%s:%s".formatted(namespace, path);
    }
}

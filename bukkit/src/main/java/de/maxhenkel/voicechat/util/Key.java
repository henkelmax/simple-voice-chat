package de.maxhenkel.voicechat.util;

import de.maxhenkel.voicechat.Voicechat;

public class Key {

    private final String namespace;
    private final String value;

    //TODO Validate allowed characters
    public Key(String namespace, String value) {
        this.namespace = namespace;
        this.value = value;
    }

    public static Key of(String namespace, String value) {
        return new Key(namespace, value);
    }

    public static Key of(String value) {
        return new Key(Voicechat.MODID, value);
    }

    public static Key parse(String value) {
        String[] split = value.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException(String.format("Invalid key: %s", value));
        }
        return new Key(split[0], split[1]);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", namespace, value);
    }

}

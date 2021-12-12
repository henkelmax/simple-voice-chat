package de.maxhenkel.voicechat.macos.jna.avfoundation;

import com.sun.jna.NativeLong;

public enum AVAuthorizationStatus {
    NOT_DETERMINED(0),
    RESTRICTED(1),
    DENIED(2),
    AUTHORIZED(3);

    private final int value;

    AVAuthorizationStatus(int value) {
        this.value = value;
    }

    public static AVAuthorizationStatus byValue(NativeLong value) {
        return byValue(value.longValue());
    }

    public static AVAuthorizationStatus byValue(long value) {
        return byValue((int) value);
    }

    public static AVAuthorizationStatus byValue(int value) {
        for (AVAuthorizationStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
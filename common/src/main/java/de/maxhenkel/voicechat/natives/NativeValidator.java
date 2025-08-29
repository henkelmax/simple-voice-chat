package de.maxhenkel.voicechat.natives;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.macos.VersionCheck;

public abstract class NativeValidator {

    private NativeState state;

    public NativeValidator() {
        state = NativeState.NOT_INITIALIZED;
    }

    protected abstract void runValidation() throws Throwable;

    protected abstract String getNativeName();

    public void initialize() {
        if (state.isInitialized()) {
            return;
        }

        if (!CrossSideManager.get().useNatives()) {
            Voicechat.LOGGER.info("Skipping initialization of {} - Natives are disabled", getNativeName());
            state = NativeState.failed("Natives are disabled");
            return;
        }

        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                Voicechat.LOGGER.info("Skipping initialization of {} - Unsupported macOS version", getNativeName());
                state = NativeState.failed("Unsupported macOS version");
                return;
            }
        }

        Voicechat.LOGGER.info("Initializing {}", getNativeName());

        Boolean success = NativeUtils.createSafe(() -> {
            runValidation();
            return true;
        }, e -> {
            Voicechat.LOGGER.warn("Failed to validate {}", getNativeName(), e);
            state = NativeState.failed(e.getMessage());
        });
        if (success == null || !success) {
            if (!state.isInitialized()) {
                state = NativeState.failed("Unknown error");
            }
            return;
        }
        state = NativeState.SUCCESS;
        Voicechat.LOGGER.info("Successfully initialized {}", getNativeName());
    }

    public void setFailed(String message) {
        state = NativeState.failed(message);
    }

    public boolean canUse() {
        if (!state.isInitialized()) {
            initialize();
        }
        return state.isSuccess();
    }

    public String getMessage() {
        return state.getMessage();
    }

    protected static enum NativeLoadState {
        NOT_INITIALIZED,
        SUCCESS,
        FAILED
    }

    protected static class NativeState {

        public static final NativeState NOT_INITIALIZED = notInitialized();
        public static final NativeState SUCCESS = success();

        private final NativeLoadState state;
        private final String message;

        private NativeState(NativeLoadState state, String message) {
            this.state = state;
            this.message = message;
        }

        private static NativeState notInitialized() {
            return new NativeState(NativeLoadState.NOT_INITIALIZED, "");
        }

        private static NativeState success() {
            return new NativeState(NativeLoadState.SUCCESS, "");
        }

        public static NativeState failed(String message) {
            return new NativeState(NativeLoadState.FAILED, message);
        }

        public NativeLoadState getState() {
            return state;
        }

        public boolean isInitialized() {
            return !NativeLoadState.NOT_INITIALIZED.equals(state);
        }

        public boolean isSuccess() {
            return NativeLoadState.SUCCESS.equals(state);
        }

        public boolean isFailed() {
            return NativeLoadState.FAILED.equals(state);
        }

        public String getMessage() {
            return message;
        }
    }

}

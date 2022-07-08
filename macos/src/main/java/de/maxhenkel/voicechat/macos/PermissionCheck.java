package de.maxhenkel.voicechat.macos;

import de.maxhenkel.voicechat.macos.jna.avfoundation.AVAuthorizationStatus;
import de.maxhenkel.voicechat.macos.jna.avfoundation.AVCaptureDevice;
import de.maxhenkel.voicechat.macos.jna.foundation.NSString;

public class PermissionCheck {
    private static final NSString AVMediaTypeAudio = new NSString("soun");

    public static void requestMicrophonePermissions() {
        checkMicrophonePermissions(true);
    }

    public static AVAuthorizationStatus getMicrophonePermissions() {
        return checkMicrophonePermissions(false);
    }

    public static AVAuthorizationStatus checkMicrophonePermissions(boolean requestIfNeeded) {
        if (!VersionCheck.isMinimumVersion(10, 15, 0)) {
            return AVAuthorizationStatus.AUTHORIZED;
        }

        var status = AVCaptureDevice.getAuthorizationStatus(AVMediaTypeAudio);
        if (requestIfNeeded && status == AVAuthorizationStatus.NOT_DETERMINED) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio);
        }

        return status;
    }
}

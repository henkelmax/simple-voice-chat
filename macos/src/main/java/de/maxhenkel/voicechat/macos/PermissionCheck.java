package de.maxhenkel.voicechat.macos;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PermissionCheck {

    public static void requestMicrophonePermissions() {
        checkMicrophonePermissions(true);
    }

    public static AVAuthorizationStatus getMicrophonePermissions() {
        return checkMicrophonePermissions(false);
    }

    public static AVAuthorizationStatus checkMicrophonePermissions(boolean requestIfNeeded) {
        if (!VersionCheck.isMinimumVersion(10, 14, 0)) {
            return AVAuthorizationStatus.AUTHORIZED;
        }
        Pointer classPointerAVCaptureDevice = AVFoundationLibrary.INSTANCE.objc_getClass("AVCaptureDevice");
        Pointer pointerGetAuthorizationStatus = AVFoundationLibrary.INSTANCE.sel_registerName("authorizationStatusForMediaType:");
        Pointer pointerRequestAccessForMediaType = AVFoundationLibrary.INSTANCE.sel_registerName("requestAccessForMediaType:completionHandler:");

        Pointer classPointerNSString = FoundationLibrary.INSTANCE.objc_getClass("NSString");
        Pointer pointerCreateNSStringWithUTF8CString = FoundationLibrary.INSTANCE.sel_registerName("stringWithUTF8String:");
        Pointer pointerAudioRequestConstant = FoundationLibrary.INSTANCE.objc_msgSend(classPointerNSString, pointerCreateNSStringWithUTF8CString, "soun");

        NativeLong permissionEnum = AVFoundationLibrary.INSTANCE.objc_msgSend(classPointerAVCaptureDevice, pointerGetAuthorizationStatus, pointerAudioRequestConstant);

        AVAuthorizationStatus avAuthorizationStatus = AVAuthorizationStatus.byValue(permissionEnum);

        if (requestIfNeeded && avAuthorizationStatus.equals(AVAuthorizationStatus.NOT_DETERMINED)) {
            AVFoundationLibrary.INSTANCE.objc_msgSend(classPointerAVCaptureDevice, pointerRequestAccessForMediaType, pointerAudioRequestConstant, null);
        }
        return avAuthorizationStatus;
    }

    public enum AVAuthorizationStatus {
        NOT_DETERMINED(0),
        RESTRICTED(1),
        DENIED(2),
        AUTHORIZED(3);

        private final int value;

        AVAuthorizationStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
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
    }

    private interface AVFoundationLibrary extends Library {
        AVFoundationLibrary INSTANCE = Native.load("AVFoundation", AVFoundationLibrary.class, Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

        // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
        Pointer objc_getClass(String className);

        // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
        Pointer sel_registerName(String selectorName);

        // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
        NativeLong objc_msgSend(Pointer receiver, Pointer selector, Pointer pointer);

        NativeLong objc_msgSend(Pointer receiver, Pointer selector, Pointer pointer1, Pointer pointer2);
    }

    private interface FoundationLibrary extends Library {
        FoundationLibrary INSTANCE = Native.load("Foundation", FoundationLibrary.class, Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

        // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
        Pointer objc_getClass(String className);

        // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
        Pointer sel_registerName(String selectorName);

        // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
        Pointer objc_msgSend(Pointer receiver, Pointer selector, String name);
    }

}

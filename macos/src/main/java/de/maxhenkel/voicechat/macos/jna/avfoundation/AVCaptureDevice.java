package de.maxhenkel.voicechat.macos.jna.avfoundation;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import de.maxhenkel.voicechat.macos.jna.foundation.NSObject;
import de.maxhenkel.voicechat.macos.jna.foundation.NSString;

public class AVCaptureDevice extends NSObject {
    // AVCaptureDevice -> https://developer.apple.com/documentation/avfoundation/avcapturedevice?language=objc
    private static final Pointer nativeClass = AVFoundation.INSTANCE.objc_getClass("AVCaptureDevice");

    // [AVCaptureDevice authorizationStatusForMediaType:]; -> https://developer.apple.com/documentation/avfoundation/avcapturedevice/1624613-authorizationstatusformediatype?language=objc
    private static final Pointer authorizationStatusForMediaTypeSelector = AVFoundation.INSTANCE.sel_registerName("authorizationStatusForMediaType:");

    // [AVCaptureDevice requestAccessForMediaType:completion:]; -> https://developer.apple.com/documentation/avfoundation/avcapturedevice/1624584-requestaccessformediatype?language=objc
    private static final Pointer requestAccessForMediaTypeSelector = AVFoundation.INSTANCE.sel_registerName("requestAccessForMediaType:completionHandler:");

    /**
     * Unused constructor, but required for inheriting our {@link NSObject} class.
     */
    public AVCaptureDevice() {
        super(new NativeLong(-1L));
    }

    /**
     * Returns the authorization status for a specified media type.
     *
     * @param mediaType A media type constant, i.e. "soun" for AVMediaTypeAudio.
     * @return DENIED if the user has explicitly denied permission for the specified media type, ACCEPTED if permission has been granted, or NOT_DETERMINED if the user has not yet made a choice regarding whether permission has been granted.
     */
    public static AVAuthorizationStatus getAuthorizationStatus(NSString mediaType) {
        NativeLong permissionEnum = AVFoundation.INSTANCE.objc_msgSend(nativeClass, authorizationStatusForMediaTypeSelector, mediaType.getId());
        return AVAuthorizationStatus.byValue(permissionEnum);
    }

    /**
     * Requests the user's permission, if needed, for recording a specified media type.
     *
     * @param mediaType A media type constant, i.e. "soun" for AVMediaTypeAudio.
     * @see <a href="https://developer.apple.com/documentation/avfoundation/avcapturedevice/1624584-requestaccessformediatype?language=objc"">...</a>
     */
    public static void requestAccessForMediaType(NSString mediaType) {
        AVFoundation.INSTANCE.objc_msgSend(nativeClass, requestAccessForMediaTypeSelector, mediaType.getId(), null);
    }
}

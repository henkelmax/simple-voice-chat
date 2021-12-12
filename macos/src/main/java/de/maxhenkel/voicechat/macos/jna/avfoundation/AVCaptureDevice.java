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

    // [AVCaptureDevice requestAccessForMediaType:completion:]; ->
    private static final Pointer requestAccessForMediaTypeSelector = AVFoundation.INSTANCE.sel_registerName("requestAccessForMediaType:completionHandler:");

    public AVCaptureDevice(NativeLong id) {
        super(id);
    }

    public static AVAuthorizationStatus getAuthorizationStatus(NSString mediaType) {
        NativeLong permissionEnum = AVFoundation.INSTANCE.objc_msgSend(nativeClass, authorizationStatusForMediaTypeSelector, mediaType.getId());
        return AVAuthorizationStatus.byValue(permissionEnum);
    }

    public static void requestAccessForMediaType(NSString mediaType) {
        AVFoundation.INSTANCE.objc_msgSend(nativeClass, requestAccessForMediaTypeSelector, mediaType.getId(), null);
    }
}

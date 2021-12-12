package de.maxhenkel.voicechat.macos.jna.foundation;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;

/**
 * A reference to a NSString object, which encapsulates a CFString reference.
 */
public class NSString extends NSObject {
    // NSString -> https://developer.apple.com/documentation/foundation/nsstring?language=objc
    private static final Pointer nativeClass = Foundation.INSTANCE.objc_getClass("NSString");

    // [NSString stringWithUTF8String:]; -> https://developer.apple.com/documentation/foundation/nsstring/1497379-stringwithutf8string?language=objc
    private static final Pointer stringWithUTF8StringSelector = Foundation.INSTANCE.sel_registerName("stringWithUTF8String:");

    /**
     * Creates a reference to a NSString object, which encapsulates a CFString reference.
     */
    public NSString(String javaString) {
        super(getNativeString(javaString));
    }

    /**
     * Wraps a reference to a NSString object, which encapsulates a CFString reference.
     */
    public NSString(NativeLong id) {
        super(id);
    }

    /**
     * Converts a Java {@link String} to a native string
     *
     * @param javaString the string to convert
     * @return a native string pointer
     */
    private static NativeLong getNativeString(String javaString) {
        return Foundation.INSTANCE.objc_msgSend(nativeClass, stringWithUTF8StringSelector, javaString);
    }

    @Override
    public String toString() {
        return getJVMString();
    }

    /**
     * Returns a Java {@link String} from an NSString instance
     *
     * @return the NSString converted to String
     */
    public String getJVMString() {
        return getCFStringRef().stringValue();
    }

    /**
     * Gets a CFString reference to the NSString's pointer
     */
    public CoreFoundation.CFStringRef getCFStringRef() {
        return new CoreFoundation.CFStringRef(new Pointer(getId().longValue()));
    }
}
package de.maxhenkel.voicechat.macos.jna.foundation;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * A reference to an NSObject, which is holds an 'identifier' (a reference) to a specific object
 */
public class NSObject {
    private static final NativeLong nullPointer = new NativeLong(0L);

    // [NSObject release]; -> https://developer.apple.com/documentation/objectivec/1418956-nsobject/1571957-release
    private static final Pointer releaseSelector = Foundation.INSTANCE.sel_registerName("release");

    protected final NativeLong id;

    public NSObject(NativeLong id) {
        this.id = id;
    }

    /**
     * Returns the identifier for this NSObject
     */
    public final NativeLong getId() {
        return id;
    }

    /**
     * Since we don't have ARC, we need to call this manually at times.
     * <p>
     * https://developer.apple.com/documentation/objectivec/1418956-nsobject/1571957-release
     */
    public void release() {
        Foundation.INSTANCE.objc_msgSend(id, releaseSelector);
    }

    public boolean isNull() {
        return id.equals(nullPointer);
    }
}

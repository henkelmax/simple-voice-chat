package de.maxhenkel.voicechat.macos.jna.foundation;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface Foundation extends Library {
    Foundation INSTANCE = Native.load("Foundation", Foundation.class);

    // void objc_msgSend(*); -> https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector);

    // void objc_msgSend(*); -> https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    NativeLong objc_msgSend(Pointer receiver, Pointer selector, String arg1);

    // id objc_getClass(const char* name); -> https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    Pointer objc_getClass(String className);

    // SEL sel_registerName(const char* name); -> https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    Pointer sel_registerName(String selectorName);
}

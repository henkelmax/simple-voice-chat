package de.maxhenkel.voicechat.macos.jna.avfoundation;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface AVFoundation extends Library {
    AVFoundation INSTANCE = Native.load("AVFoundation", AVFoundation.class);

    // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    Pointer objc_getClass(String className);

    // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    Pointer sel_registerName(String selectorName);

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    NativeLong objc_msgSend(Pointer receiver, Pointer selector, NativeLong arg1);

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    NativeLong objc_msgSend(Pointer receiver, Pointer selector, NativeLong arg1, Pointer arg2);
}
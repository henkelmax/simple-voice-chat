# MacOS Microphone Permission Hack

This submodule contains the code to check if microphone permissions are granted.
It also has a hacky method to bypass this restriction.

### How does this workaround work?

For a MacOS application to get microphone permissions, the following things must be given:

- The applications `Info.plist` needs a `NSMicrophoneUsageDescription`. More information [here](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW25).
- If the application is signed (which is the case for the vanilla launcher), it needs to be signed with a microphone entitlement.

If any of these requirements is not given, and the application requests access for the microphone, MacOS will terminate the application. More information [here](https://developer.apple.com/documentation/avfoundation/cameras_and_media_capture/requesting_authorization_for_media_capture_on_ios?language=objc).

Because Simple Voice Chat itself does not know if it is allowed to ask for microphone permissions, the mod spawns another process to ask for the permission.
If this process terminates with a non-zero exit code, it is apparent that the launcher does not meet the requirement.

In this case the mod will open a GUI where you can drag your launcher application on.
This will parse the applications `Info.plist` and add the `NSMicrophoneUsageDescription` key.

It will also execute the command `codesign --deep --remove-signature <your-application>` to remove the signature of the launcher.
Note that this might cause other issues with your launcher, so use this at your own risk.
You can revert these changes by reinstalling the launcher.

After restarting the launcher, Minecraft should now ask for microphone access.

### Are there any alternatives to this?

This issue mostly affects the vanilla Minecraft launcher and the CurseForge launcher.
You can use [MultiMC](https://multimc.org/) without needing to do this workaround, since this application is not signed.
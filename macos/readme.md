# MacOS Microphone Permission Hack

This submodule contains the code to check if microphone permissions are granted.
It also has a hacky method to bypass this restriction.

### How does this workaround work?

For a MacOS application to get microphone permissions, the following things must be given:

- The applications `Info.plist` needs a `NSMicrophoneUsageDescription`. More information [here](https://developer.apple.com/documentation/bundleresources/information_property_list/nsmicrophoneusagedescription?language=objc).
- If the application is signed (which is the case for the vanilla launcher), it needs to be signed with a microphone [entitlement](https://developer.apple.com/documentation/bundleresources/entitlements?language=objc).

If any of these requirements is not given, and the application requests access for the microphone, MacOS will terminate the application. More information [here](https://developer.apple.com/documentation/avfoundation/cameras_and_media_capture/requesting_authorization_for_media_capture_on_macos?language=objc).

Because Simple Voice Chat itself does not know if it is allowed to ask for microphone permissions, the mod spawns another process to ask for the permission.
If this process terminates with a non-zero exit code, it is apparent that the launcher does not meet the requirement.

In this case the mod will open a GUI where you can drag your launcher application on.
This will parse the applications `Info.plist` and add the `NSMicrophoneUsageDescription` key.

It will also execute the command `codesign --deep --remove-signature <your-application>` to remove the signature of the launcher.
Note that this might cause other issues with your launcher, so use this at your own risk.
You can revert these changes by reinstalling the launcher.

After restarting the launcher, Minecraft should now ask for microphone access.

### How to patch your launcher

**Vanilla Minecraft Launcher**
- Drag the minecraft launcher from your applications folder on the patcher GUI

**CurseForge Launcher**
- Go to `Settings` > `Addons` > `Mod installation path` and open that path (The default is `Documents/curseforge/`)
- Open the `minecraft` folder in the mod installation path
- Open the `Install` folder
- Drag the `Minecraft` application on the patcher GUI

**MultiMC**
- Drag MultiMC from your applications folder on the patcher GUI

### Troubleshooting Problems

If the launcher doesn't ask for Microphone access after patching it in the GUI, run the following command in a terminal window:
`codesign --force --deep --sign - <your-application>` where `<your-application>` is the path to your launcher.


If there is a popup `Minecraft wants to use your confidential information stored in "mojangTokenService" in your keychain`,
enter the password of your Mac and click on `Always Allow`.
If the popup still constantly pops up, restart your computer and try again.

If patching your launcher fails with the error `xcrun: error: invalid active developer path`,
run the command `sudo xcode-select --install` in your terminal.

### Are there any alternatives to this?

You can run your launcher in the terminal.
This will cause the microphone permission popup to work.
The downside of this is that you need to do this everytime you want to launch Minecraft.
It will also not work with all launchers.

### Standalone Version

There is also a [standalone version](https://github.com/henkelmax/simple-voice-chat/files/7926761/simple-voice-chat-macos-workaround.zip) of the workaround.
This works exactly like the one that's built into the mod.
Use this if the GUI does not show up when starting the game or your microphone is not picking up sound.
You also need to use this version if you are using `Minecraft 1.16.5` or older, since there is no builtin patcher in these versions.

*Note* that you need to open the `.jar` with `Java 17`.

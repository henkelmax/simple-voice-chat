![](http://cf.way2muchnoise.eu/full_416089_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/416089.svg) ![](https://img.shields.io/discord/854659575324344340?label=Discord&style=flat&logo=discord&labelColor=2d2d2d)

# Simple Voice Chat

## Links
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat)
- [Spigot](https://www.spigotmc.org/resources/simple-voice-chat.93738/)
- [Discord](https://discord.gg/4dH2zwTmyX)
- [Wiki](https://modrepo.de/minecraft/voicechat/wiki)
- [ModRepo](https://modrepo.de/minecraft/voicechat/overview)
- [GitHub](https://github.com/henkelmax/simple-voice-chat)
- [FAQ](https://modrepo.de/minecraft/voicechat/faq)
- [Credits](https://modrepo.de/minecraft/voicechat/credits)

---

<p align="center">
    <a href="https://discord.gg/4dH2zwTmyX">
        <img src="https://i.imgur.com/JgDt1Fl.png" width="300">
    </a>
</p>
<p align="center">
    <i>Please join the Discord if you have questions!</i>
</p>

This mod adds a proximity voice chat to your Minecraft server.
You can choose between push to talk (PTT) or voice activation.
The default PTT key is `CAPS LOCK`, but it can be changed in the controls.
When using voice activation, you can mute your microphone by pressing the `M` key.
You can access the voice chat settings by pressing the `V` key.

## Features

- Proximity voice chat
- Password protected group chats
- [Opus codec](https://opus-codec.org/)
- [RNNoise](https://jmvalin.ca/demo/rnnoise/) recurrent neural network noise suppression
- OpenAL audio
- Cross compatibility between Fabric, Forge, Bukkit, Spigot and Paper
- Compatibility with [Sound Physics Remastered](https://www.curseforge.com/minecraft/mc-mods/sound-physics-remastered)
- Compatibility with [Sound Physics Fabric](https://www.curseforge.com/minecraft/mc-mods/sound-physics-fabric) (Not recommended)
- Compatibility with [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu) (Use [ClothConfig](https://www.curseforge.com/minecraft/mc-mods/cloth-config) for a better configuration UI)
- Push to talk
- Voice activation
- Configurable PTT key
- Test microphone playback
- Indicator on the screen when you are talking
- Indicator next to players names when they are talking
- Configurable voice distance
- Whispering
- Mute other players
- Adjust the volume of other players
- Microphone amplification
- 3D sound
- AES encryption
- Configurable network port
- Audio recording

## Icons

When other players talk in the voice chat, you see a little speaker icon next to their name.

![](https://i.imgur.com/tDmw2vz.png)

When you are talking (Either PTT or voice activation),
you see a little microphone icon in the bottom left corner of your screen.

![](https://i.imgur.com/7ov5D6S.png)

If a player deactivated their voice chat, you see a striked out icon next to their name.

![](https://i.imgur.com/bo9bJZk.png)

You are seeing the same icon in the bottom left corner of your screen if you deactivate the voice chat yourself.

![](https://i.imgur.com/NTzjYcf.png)

If you mute your microphone (Voice activation only), you will also see an indicator icon on your screen.

![](https://i.imgur.com/2qUSJTe.png)

If you or another player loses the connection to the voice chat they will have an icon indicating that.
You will also see this icon if a player does not have this mod installed.

![](https://i.imgur.com/PIgilvR.png)

## Key Bindings

Name | Default Key | Description
--- | --- | ---
Voice Chat GUI | `V` | Opens the voice chat GUI.
Voice Chat Settings | *Not bound by default* | Opens the voice chat settings.
Group Chats | `G` | Opens the group chat GUI.
Push To Talk | `CAPS LOCK` | The push to talk key (Only when using activation type `PTT`).
Mute Microphone | `M` | The mute button (Only when using voice activation type `Voice`).
Disable Voice Chat | `N` | This button disables the voice chat. Other people can't hear you and you can't hear them.
Hide Voice Chat Icons | `H` | This button hides all icons related to the voice chat. This does not affect any other functionalities.
Toggle Recording | *Not bound by default* | Toggles voice chat audio recording.
Whisper | *Not bound by default* | Hold down to whisper.

## The GUI

You can open the voice chat GUI by pressing the `V` key.
This allows you to open the settings, group chats, mute yourself, disable the voice chat and hide all icons.

![](https://i.imgur.com/I0u3tx2.png)

### Group Chats

Group chats allow you to talk to players that are not in your vicinity.
To open the group chat interface, either press the group button in the voice chat GUI or just press the `G` key.

To create a new group, just type a name in the text field and press the button next to it.

![](https://i.imgur.com/OD1kRk8.png)

To join an already existing group, click the group in the list below the text field.

![](https://i.imgur.com/OygMT1S.png)

Creating or joining a group will bring you into the group chat interface.
You will also see the heads of the group members in the top left corner of your screen.
Talking players will be outlined.
You can disable these icons by pressing the third button from the left.

![](https://i.imgur.com/XT9AiPQ.png)

Players that are not in a group will see a group icon next to your head, indicating that they can't talk to you.

You can invite players to your group chat by entering the command `/voicechat invite <playername>`.

### Settings

You can access the voice chat GUI by pressing the `V` key and pressing the settings button.

This menu offers the ability to change the general voice chat volume and your microphone amplification.
In addition, there is the possibility to specify the recording and playback device.

By clicking the 'Enable microphone testing' button,
you can hear your own voice and adjust the activation level of the voice activation.

![](https://i.imgur.com/aMHIVjY.png)

By pressing the 'Adjust player volumes' in the voice chat settings,
you can adjust the individual volumes of each player.

![](https://i.imgur.com/S8Iwatm.png)

## Important Notes

You need to open a port on the server.
This is port `24454` `UDP` by default.
Without opening this port, the voice chat will not work.
This port can be changed in the server config.
More information [here](https://modrepo.de/minecraft/voicechat/wiki?t=setup).

This mod does only work when connected to a dedicated server.
You need to have this mod installed on the server and the client for it to work.

The Fabric version of this mod allows you to join with vanilla clients,
but you won't be able to use the voice chat features.

The voice chat is encrypted, but I don't guarantee the security of it.
Use at your own risk!

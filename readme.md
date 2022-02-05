![](http://cf.way2muchnoise.eu/full_416089_downloads.svg)
![](http://cf.way2muchnoise.eu/versions/416089.svg)
![](https://img.shields.io/discord/854659575324344340?label=Discord&style=flat&logo=discord&labelColor=2d2d2d)

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat)
|
[CurseForge Bukkit](https://www.curseforge.com/minecraft/bukkit-plugins/simple-voice-chat/files/all)
|
[Spigot](https://www.spigotmc.org/resources/simple-voice-chat.93738/)
|
[Modrinth](https://modrinth.com/mod/simple-voice-chat)
|
[Discord](https://discord.gg/4dH2zwTmyX)
|
[Wiki](https://modrepo.de/minecraft/voicechat/wiki)
|
[FAQ](https://modrepo.de/minecraft/voicechat/faq)
|
[Credits](https://modrepo.de/minecraft/voicechat/credits)
|
[Plugin API](api/readme.md)

# Simple Voice Chat

This mod adds a proximity voice chat to your Minecraft server.
You can choose between push to talk (PTT) or voice activation.
The default PTT key is `CAPS LOCK`, but it can be changed in the key bind settings.
You can access the voice chat settings by pressing the `V` key.

:warning: **NOTE** This mod requires special setup on the server in order to work.
Please read the [wiki](https://modrepo.de/minecraft/voicechat/wiki?t=setup) for more information.

<p align="center">
    <a href="https://discord.gg/4dH2zwTmyX">
        <img src="https://i.imgur.com/JgDt1Fl.png" width="300">
    </a>
    <br/>
    <i>Please join the Discord if you have questions!</i>
</p>

## Downloads

- [Fabric](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat/files/all?filter-status=1&filter-game-version=2020709689:7499)
- [Forge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat/files/all?filter-status=1&filter-game-version=2020709689:7498)
- [Bukkit/Spigot/Paper](https://www.curseforge.com/minecraft/bukkit-plugins/simple-voice-chat/files/all)

## Features

- Proximity voice chat
- Password protected group chats
- [Opus codec](https://opus-codec.org/)
- [RNNoise](https://jmvalin.ca/demo/rnnoise/) recurrent neural network noise suppression
- OpenAL audio
- Cross compatibility between Fabric, Forge, Bukkit, Spigot and Paper
- Compatibility with [Sound Physics Remastered](https://www.curseforge.com/minecraft/mc-mods/sound-physics-remastered)
- Compatibility with [Sound Physics Fabric](https://www.curseforge.com/minecraft/mc-mods/sound-physics-fabric)
- Compatibility with [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu) (Use [ClothConfig](https://www.curseforge.com/minecraft/mc-mods/cloth-config) for a better configuration UI)
- Push to talk
- Voice activation
- Configurable push to talk key
- Microphone test playback
- Configurable voice distance
- Whispering
- Individual player volume adjustment
- Microphone amplification
- 3D sound
- AES encryption
- Audio recording with separate audio tracks
- [Plugin API](api/readme.md)

## Icons

|                  Icon                   | Description                                           |
|:---------------------------------------:|-------------------------------------------------------|
|  ![](https://i.imgur.com/FZD3ohs.png)   | You are talking                                       |
|  ![](https://i.imgur.com/BJt2YAL.png)   | You are whispering                                    |
|  ![](https://i.imgur.com/lmN6ydy.png)   | Player is talking                                     |
|  ![](https://i.imgur.com/Felj73b.png)   | Player is whispering                                  |
|  ![](https://i.imgur.com/dI3pfmA.png)   | Microphone muted                                      |
|  ![](https://i.imgur.com/MZRBqra.png)   | Voice chat disabled                                   |
|  ![](https://i.imgur.com/Lv3K6tC.png)   | Voice chat not connected<br/>Voice chat not installed |

## The GUI

You can open the voice chat GUI by pressing the `V` key.
This allows you to open the settings, group chats, mute yourself, disable the voice chat, start/stop a recording and hide all icons.

![](https://i.imgur.com/TCCHTl8.png)

### Group Chats

Group chats allow you to talk to players that are not in your vicinity.
To open the group chat interface, either press the group button in the voice chat GUI or just press the `G` key.

To create a new group, just type a name in the text field and press the button next to it.

![](https://i.imgur.com/FihRdNd.png)

Creating or joining a group will bring you into the group chat interface.
You will also see the heads of the group members in the top left corner of your screen.
Talking players will be outlined.
You can disable these icons by pressing the third button from the left.

![](https://i.imgur.com/sYfa350.png)

Players that are not in a group will see a group icon next to your head, indicating that they can't talk to you.

You can invite players to your group chat by entering the command `/voicechat invite <playername>`.

### Settings

You can access the voice chat GUI by pressing the `V` key and pressing the settings button.

This menu offers the ability to change the general voice chat volume and your microphone amplification.
In addition, there is the possibility to specify the recording and playback device.

By clicking the 'Enable microphone testing' button, you can hear your own voice and adjust the activation level of the voice activation.

![](https://i.imgur.com/D8GUick.png)

## Key Bindings

| Name                  |      Default Key       | Description                                                                               |
|-----------------------|:----------------------:|-------------------------------------------------------------------------------------------|
| Voice Chat GUI        |          `V`           | Opens the voice chat GUI.                                                                 |
| Voice Chat Settings   | *Not bound by default* | Opens the voice chat settings.                                                            |
| Group Chats           |          `G`           | Opens the group chat GUI.                                                                 |
| Push To Talk          |      `CAPS LOCK`       | The push to talk key (Only when using activation type `PTT`).                             |
| Mute Microphone       |          `M`           | The mute button (Only when using voice activation type `Voice`).                          |
| Disable Voice Chat    |          `N`           | This button disables the voice chat. Other people can't hear you and you can't hear them. |
| Hide Voice Chat Icons |          `H`           | This button hides all icons related to the voice chat.                                    |
| Toggle Recording      | *Not bound by default* | Toggles voice chat audio recording.                                                       |
| Whisper               | *Not bound by default* | Hold down to whisper.                                                                     |

You can change every key binding in the Minecraft key binding settings.

## Important Notes

You need to open a port on the server. This is port `24454` `UDP` by default.
Without opening this port, the voice chat will not work.
This port can be changed in the server config.
More information [here](https://modrepo.de/minecraft/voicechat/wiki?t=setup).

The voice chat is encrypted, but we don't guarantee the security of it. Use at your own risk!

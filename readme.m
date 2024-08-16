[Modrinth](https://modrinth.com/mod/simple-voice-chat)
|
[CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/simple-voice-chat)
|
[Discord](https://discord.gg/4dH2zwTmyX)
|
[Wiki](https://modrepo.de/minecraft/voicechat/wiki)
|
[FAQ](https://modrepo.de/minecraft/voicechat/faq)
|
[Credits](https://modrepo.de/minecraft/voicechat/credits)
|
[API](https://modrepo.de/minecraft/voicechat/api)

# Simple Voice Chat

A proximity voice chat for Minecraft with a variety of [addons](https://modrepo.de/minecraft/voicechat/addons) that offer additional features and functionalities.

:warning: **NOTE** This mod requires special setup on the server in order to work.
Please read the [wiki](https://modrepo.de/minecraft/voicechat/wiki/setup) for more information.

<p align="center">
    <a href="https://discord.gg/4dH2zwTmyX">
        <img src="https://i.imgur.com/JgDt1Fl.png" width="300">
    </a>
    <br/>
    <i>Please join the Discord if you have questions!</i>
</p>

## Downloads

- [Fabric](https://modrinth.com/mod/simple-voice-chat/versions?l=fabric)
- [NeoForge](https://modrinth.com/mod/simple-voice-chat/versions?l=neoforge)
- [Forge](https://modrinth.com/mod/simple-voice-chat/versions?l=forge)
- [Bukkit/Spigot/Paper](https://modrinth.com/plugin/simple-voice-chat/versions?l=bukkit)
- [Quilt](https://modrinth.com/mod/simple-voice-chat/versions?l=quilt)
- [Velocity](https://modrinth.com/mod/simple-voice-chat/versions?l=velocity)
- [BungeeCord/Waterfall](https://modrinth.com/mod/simple-voice-chat/versions?l=bungeecord)

## Features

- Push to talk
- Voice activation
- Proximity voice chat
- Password protected group chats
- [Opus codec](https://opus-codec.org/)
- [RNNoise](https://jmvalin.ca/demo/rnnoise/) recurrent neural network noise suppression
- OpenAL audio
- Cross compatibility between Fabric, NeoForge, Forge, Quilt, Bukkit, Spigot and Paper
- Support for Velocity, BungeeCord and Waterfall
- Compatibility with [ModMenu](https://modrinth.com/mod/modmenu) (Use [ClothConfig](https://modrinth.com/mod/cloth-config) for a better configuration UI)
- Configurable push to talk key
- Microphone test playback
- Configurable voice distance
- Whispering
- Individual player volume adjustment
- Microphone amplification
- 3D sound
- AES encryption
- Audio recording with separate audio tracks
- A powerful [API](https://modrepo.de/minecraft/voicechat/api)
- Many [addons](https://modrepo.de/minecraft/voicechat/addons)

## Icons

|                 Icon                 | Description                                           |
|:------------------------------------:|-------------------------------------------------------|
| ![](https://i.imgur.com/FZD3ohs.png) | You are talking                                       |
| ![](https://i.imgur.com/BJt2YAL.png) | You are whispering                                    |
| ![](https://i.imgur.com/lmN6ydy.png) | Player is talking                                     |
| ![](https://i.imgur.com/Felj73b.png) | Player is whispering                                  |
| ![](https://i.imgur.com/dI3pfmA.png) | Microphone muted                                      |
| ![](https://i.imgur.com/MZRBqra.png) | Voice chat disabled                                   |
| ![](https://i.imgur.com/Lv3K6tC.png) | Voice chat not connected<br/>Voice chat not installed |

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

![](https://i.imgur.com/ZVSfBms.png)

Players that are not in a group will see a group icon next to your head, indicating that they can't talk to you.

You can invite players to your group chat by entering the command `/voicechat invite <playername>`.

### Settings

You can access the voice chat GUI by pressing the `V` key and pressing the settings button.

This menu offers the ability to change the general voice chat volume and your microphone amplification.
In addition, there is the possibility to specify the recording and playback device.

By clicking the 'Enable microphone testing' button, you can hear your own voice and adjust the activation level of the voice activation.

![](https://i.imgur.com/TMyfSYU.png)

## Important Notes

You need to open a port on the server. This is port `24454` `UDP` by default.
Without opening this port, the voice chat will not work.
This port can be changed in the server config.
More information [here](https://modrepo.de/minecraft/voicechat/wiki/setup).

The voice chat is encrypted, but we don't guarantee the security of it. Use at your own risk!

# Simple Voice Chat ![](http://cf.way2muchnoise.eu/full_416089_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/416089.svg)

## Links
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat)
- [Wiki](https://modrepo.de/minecraft/voicechat/wiki)
- [ModRepo](https://modrepo.de/minecraft/voicechat/overview)
- [GitHub](https://github.com/henkelmax/simple-voice-chat)
- [FAQ](https://modrepo.de/minecraft/voicechat/faq)

---

This mod adds a voice chat to your Minecraft server.
You can choose between push to talk (PTT) or voice activation.
The default PTT key is `CAPS LOCK`, but it can be changed in the controls.
When using voice activation, you can mute your microphone by pressing the `M` key.
You can access the voice chat settings by pressing the `V` key.

## Features

- Proximity voice chat
- Push to talk
- Voice activation
- Configurable PTT key
- Test microphone playback
- Indicator on the screen when you are talking
- Indicator next to players names when they are talking
- Configurable distance
- Mute other players
- Adjust the volume of other players
- Microphone amplification
- Semi 3D sound
- Configurable sample rate
- Configurable network port

## Voice indicator icons

When other players talk in the voice chat, you see a little speaker icon next to their name.

When you are talking (Either PTT or voice activation),
you see a little microphone icon in the bottom left corner of your screen.

![](https://i.imgur.com/cbIz2sB.png)

![](https://i.imgur.com/9w49fE6.png)

## Settings

By clicking the 'Enable microphone testing' button,
you can hear your own voice and adjust the activation level of the voice activation.

![](https://i.imgur.com/EWWngSq.png)

By pressing the 'Adjust player volumes' in the voice chat settings,
you can adjust the individual volumes of each player.

![](https://i.imgur.com/ZCDcQyx.png)

## Important Notes

You need to open a port on the server.
This is port `24454/udp` by default.
Without opening this port, the voice chat will not work.
This port can be changed in the server config.
More information [here](https://modrepo.de/minecraft/voicechat/wiki?t=setup).

This mod does only work when connected to a dedicated server.
You need to have this mod installed on the server and the client.

The voice chat is NOT encrypted.
Use at your own risk!

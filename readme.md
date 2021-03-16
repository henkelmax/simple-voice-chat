# Simple Voice Chat ![](http://cf.way2muchnoise.eu/full_416089_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/416089.svg)

## Links
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat)
- [Wiki](https://modrepo.de/minecraft/voicechat/wiki)
- [ModRepo](https://modrepo.de/minecraft/voicechat/overview)
- [GitHub](https://github.com/henkelmax/simple-voice-chat)
- [FAQ](https://modrepo.de/minecraft/voicechat/faq)

---

This mod adds a proximity voice chat to your Minecraft server.
You can choose between push to talk (PTT) or voice activation.
The default PTT key is `CAPS LOCK`, but it can be changed in the controls.
When using voice activation, you can mute your microphone by pressing the `M` key.
You can access the voice chat settings by pressing the `V` key.

## Features

- Proximity voice chat
- [Opus Codec](https://opus-codec.org/)
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

## Icons

When other players talk in the voice chat, you see a little speaker icon next to their name.

![](https://i.imgur.com/5V3uYsc.png)

When you are talking (Either PTT or voice activation),
you see a little microphone icon in the bottom left corner of your screen.

![](https://i.imgur.com/c6DqeUj.png)

If a player deactivated their voice chat, you see a striked out icon next to their name.

![](https://i.imgur.com/PDehuc0.png)

You are seeing the same icon in the bottom left corner of your screen if you deactivate the voice chat yourself.

![](https://i.imgur.com/T9S3yhq.png)

If you mute your microphone (Voice activation only), you will also see an indicator icon on your screen.

![](https://i.imgur.com/kSTfK3D.png)

If you or another player loses the connection to the voice chat they will have an icon indicating that.
You will also see this icon if a player does not have this mod installed.

![](https://i.imgur.com/J13ncwN.png)

## Key Bindings

Name | Default Key | Description
---|---|---
Voice Chat Settings|`V`|This key opens the voice chat settings menu.
Push To Talk|`CAPS LOCK`|The push to talk key (Only when using activation type `PTT`).
Mute Microphone | `M` | The mute button (Only when using voice activation type `Voice`).
Disable Voice Chat | `N` | This button disables the voice chat. Other people can't hear you and you can't hear them.
Hide Voice Chat Icons | `H` | This button hides all icons related to the voice chat. This does not affect any other functionalities.

## Settings

You can access the voice chat settings by pressing the `V` key.

This menu offers the ability to change the general voice chat volume and your microphone amplification.
In addition, there is the possibility to specify the recording and playback device.

By clicking the 'Enable microphone testing' button,
you can hear your own voice and adjust the activation level of the voice activation.

![](https://i.imgur.com/td2e5ep.png)

By pressing the 'Adjust player volumes' in the voice chat settings,
you can adjust the individual volumes of each player.

![](https://i.imgur.com/JFQn5Pf.png)

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

# Introduction

> Make sure you set up your workspace as shown in [Getting Started](getting_started.md)!


## Your Plugin

As already shown in [Getting Started](getting_started.md),
the main entrypoint for all mod loaders and plugin loaders (Fabric/Forge/Bukkit/Spigot/Paper) is the `VoicechatPlugin` interface.

```java
package com.example.yourmod;

import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class TestPlugin implements VoicechatPlugin {
    ...
}
```

The only mandatory method you need to implement is `getPluginId`.
Make sure your plugin ID is unique.
Best practice is to use the mod ID/plugin ID of your mod/plugin.

```java
@Override
public String getPluginId() {
    return "my_plugin";
}
```

Once your plugin is loaded by the voice chat, the `initialize` method of your plugin gets called.
Here you get access to the [VoicechatApi](https://voicechat.modrepo.de/de/maxhenkel/voicechat/api/VoicechatApi.html) class.

```java
@Override
public void initialize(VoicechatApi api) {
    ...
}
```

## Registering Events

You can only register events in the `registerEvents` method.
Trying to call `EventRegistration.registerEvent` after this method call will not work.

```java
@Override
public void registerEvents(EventRegistration registration) {
    registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
}

@Override
public void onServerStarted(VoicechatServerStartedEvent event) {
    System.out.println(event.getVoicechat());
}
```

If you want to give your event a higher or lower priority, you can do this by passing an integer to the `registerEvent` method.
Higher numbers mean a higher priority and thus an earlier invocation than registered events without a priority.
`0` is the default priority.

```java
@Override
public void registerEvents(EventRegistration registration) {
    registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted, 100);
}

@Override
public void onServerStarted(VoicechatServerStartedEvent event) {
    System.out.println(event.getVoicechat());
}
```

A list of all events can be found [here](https://voicechat.modrepo.de/de/maxhenkel/voicechat/api/events/package-summary.html).
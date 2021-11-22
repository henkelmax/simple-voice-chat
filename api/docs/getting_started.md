# Getting Started

## Fabric

To add the API dependency to your Fabric mod, add the following maven repository to your project.

*build.gradle*

```groovy
repositories {
    ...
    maven { url = 'https://maven.maxhenkel.de/repository/public' }
}
```

In the `dependencies` section add the following maven dependency. Make sure `voicechat_api_version` is set to the voice
chat API version you want to target.

*build.gradle*

```groovy
dependencies {
    ...
    implementation "de.maxhenkel.voicechat:voicechat-api:${voicechat_api_version}"
}
```

To avoid crashes due to outdated voice chat versions, make sure to add `voicechat` to the `depends` section in
the `fabric.mod.json`. This prevents your mod from loading if versions of the voice chat are installed, that are older
than the API version you are targeting.

*fabric.mod.json*

```json
{
  "schemaVersion": 1,
  ...
  "depends": {
    ...
    "voicechat": ">=${minecraft_version}-${voicechat_api_version}"
  }
}
```

To register the voice chat plugin for a Fabric mod, you need to create a class that
implements `de.maxhenkel.voicechat.api.VoicechatPlugin`.

*TestPlugin.java*

```java
package com.example.yourmod;

import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class TestPlugin implements VoicechatPlugin {
    ...
}
```

Additionally, you need to add a `voicechat` entrypoint in your `fabric.mod.json`, that refers to the class you just
created.

*fabric.mod.json*

```json
{
  "schemaVersion": 1,
  ...
  "entrypoints": {
    "main": [
      ...
    ],
    "voicechat": [
      "com.example.yourmod.TestPlugin"
    ]
  },
  ...
}
```

To have *Simple Voice Chat* installed in your development environment, you can either drop the mod `.jar` into
the `mods` folder, or use [Cursemaven](https://www.cursemaven.com/).

*build.gradle*

```groovy
repositories {
    ...
    maven { url = 'https://cursemaven.com' }
}
dependencies {
    ...
    modImplementation "curse.maven:simple-voice-chat-416089:${voicechat_curseforge_file_id}"
}
```

## Forge

To add the API dependency to your Forge mod, add the following maven repository to your project.

*build.gradle*

```groovy
repositories {
    ...
    maven { url = 'https://maven.maxhenkel.de/repository/public' }
}
```

In the `dependencies` section add the following maven dependency. Make sure `voicechat_api_version` is set to the voice
chat API version you want to target.

*build.gradle*

```groovy
dependencies {
    ...
    implementation "de.maxhenkel.voicechat:voicechat-api:${voicechat_api_version}"
}
```

To avoid crashes due to outdated voice chat versions, make sure to add `voicechat` to the `dependencies` section in
the `mods.toml`. This prevents your mod from loading if versions of the voice chat are installed, that are older than
the API version you are targeting.

*mods.toml*

```toml
modLoader="javafml"
...
[[mods]]
...
[[dependencies.yourmod]]
    modId="voicechat"
    mandatory=true
    versionRange="[${minecraft_version}-${voicechat_api_version},)"
    ordering="AFTER"
    side="BOTH"
```

To register the voice chat plugin for a Forge mod, you need to create a class that
implements `de.maxhenkel.voicechat.api.VoicechatPlugin` and has the
annotation `de.maxhenkel.voicechat.api.ForgeVoicechatPlugin`.

*TestPlugin.java*

```java
package com.example.yourmod;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

@ForgeVoicechatPlugin
public class TestPlugin implements VoicechatPlugin {
    ...
}
```

To have *Simple Voice Chat* installed in your development environment, use [Cursemaven](https://www.cursemaven.com/).

*build.gradle*

```groovy
repositories {
    ...
    maven { url = 'https://cursemaven.com' }
}
dependencies {
    ...
    implementation fg.deobf("curse.maven:simple-voice-chat-416089:${voicechat_curseforge_file_id}")
}
```

## Bukkit/Spigot/Paper

To add the API dependency to your Bukkit/Spigot/Paper plugin add the following maven repository to your project.

*build.gradle*

```groovy
repositories {
    ...
    maven { url = 'https://maven.maxhenkel.de/repository/public' }
}
```

In the `dependencies` section add the following maven dependency. Make sure `voicechat_api_version` is set to the voice
chat API version you want to target.

*build.gradle*

```groovy
dependencies {
    ...
    implementation "de.maxhenkel.voicechat:voicechat-api:${voicechat_api_version}"
}
```

To make your plugin depend on the voice chat plugin add the following to your `plugin.yml`.

*plugin.yml*

```yml
...
depend: [ voicechat ]
```

To register the voice chat plugin for a Bukkit/Spigot/Paper plugin, you need to create a class that
implements `de.maxhenkel.voicechat.api.VoicechatPlugin`.

*TestPlugin.java*

```java
package com.example.yourplugin;

import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class TestPlugin implements VoicechatPlugin {
    ...
}
```

Additionally, you need to register it in the `BukkitVoicechatService`.

```java
package com.example.yourplugin;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(new TestPlugin());
        }
    }
    
    ...
}
```
package de.maxhenkel.voicechat.integration.commodore;

import com.mojang.brigadier.arguments.ArgumentType;
import me.lucko.commodore.MinecraftArgumentTypes;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Constructor;

public class UUIDArgument {

    public static ArgumentType<?> uuid() {
        return newUUIDArgument(NamespacedKey.minecraft("uuid"));
    }

    private static ArgumentType<?> newUUIDArgument(NamespacedKey key, Object... args) {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = MinecraftArgumentTypes.getClassByKey(key).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}


package de.maxhenkel.voicechat.integration.commodore;

import com.mojang.brigadier.arguments.ArgumentType;
import me.lucko.commodore.MinecraftArgumentTypes;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Constructor;

public class EntityArgument {

    public static ArgumentType<?> entity(boolean single, boolean playerOnly) {
        return newEntityArgument(NamespacedKey.minecraft("entity"), single, playerOnly);
    }

    private static ArgumentType<?> newEntityArgument(NamespacedKey key, Object... args) {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = MinecraftArgumentTypes.getClassByKey(key).getDeclaredConstructor(boolean.class, boolean.class);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}


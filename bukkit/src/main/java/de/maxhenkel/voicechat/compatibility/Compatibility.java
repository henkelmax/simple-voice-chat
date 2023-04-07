package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Compatibility {

    String getServerIp(Server server) throws Exception;

    void addChannel(Player player, String channel);

    void removeChannel(Player player, String channel);

    NamespacedKey createNamespacedKey(String key);

    void sendMessage(Player player, Component component);

    void sendStatusMessage(Player player, Component component);

    void runTask(Runnable runnable);

    ArgumentType<?> playerArgument();

    ArgumentType<?> uuidArgument();

    default <T> T callMethod(Object object, String methodName) {
        return callMethod(object, methodName, new Class[]{});
    }

    default <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, methodName, parameterTypes, args);
    }

    default <T> T callMethod(Class<?> clazz, Object object, String methodName) {
        return callMethod(clazz, object, methodName, new Class[]{});
    }

    default <T> T callMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (T) method.invoke(object, args);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default <T> T callMethod(Class<?> object, String methodName) {
        return callMethod(object, methodName, new Class[]{});
    }

    default <T> T callMethod(Class<?> object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object, null, methodName, parameterTypes, args);
    }

    default <T> T callConstructor(Class<?> object) {
        return callConstructor(object, new Class[]{});
    }

    default <T> T callConstructor(Class<?> object, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<?> constructor = object.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default <T> T getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default <T> T getField(Class<?> object, String fieldName) {
        try {
            Field field = object.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default Class<?> getClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

    default boolean doesClassExist(String... classNames) {
        for (String className : classNames) {
            try {
                Class.forName(className);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    default boolean doesMethodExist(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

}

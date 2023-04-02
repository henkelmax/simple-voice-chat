package de.maxhenkel.voicechat.compatibility;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public interface Compatibility {

    String getServerIp(Server server) throws Exception;

    void addChannel(Player player, String channel);

    void removeChannel(Player player, String channel);

    NamespacedKey createNamespacedKey(String key);

    void sendMessage(Player player, Component component);

    void sendStatusMessage(Player player, Component component);

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
            return (T) clazz.getDeclaredMethod(methodName, parameterTypes).invoke(object, args);
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
            return (T) object.getDeclaredConstructor(parameterTypes).newInstance(args);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default <T> T getField(Object object, String fieldName) {
        try {
            return (T) object.getClass().getDeclaredField(fieldName).get(object);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    default <T> T getField(Class<?> object, String fieldName) {
        try {
            return (T) object.getDeclaredField(fieldName).get(null);
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

}

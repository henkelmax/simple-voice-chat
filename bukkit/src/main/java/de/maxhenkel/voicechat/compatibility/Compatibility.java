package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Compatibility {

    default void init() throws Exception {

    }

    void addChannel(Player player, String channel);

    void removeChannel(Player player, String channel);

    Key createNamespacedKey(String key);

    void sendJsonMessage(Player player, String json);

    void sendJsonStatusMessage(Player player, String json);

    void sendTranslationMessage(Player player, String key, String... args);

    void sendStatusMessage(Player player, String key, String... args);

    String createTranslationMessage(String key, String... args);

    void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand);

    void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName);

    void runTask(Runnable runnable);

    void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period);

    ArgumentType<?> playerArgument();

    ArgumentType<?> uuidArgument();

    String getBaseBukkitPackage();

    String getBaseServerPackage();

    default <T> T callMethod(Object object, String... methodNames) {
        return callMethod(object, methodNames, new Class[]{});
    }

    default <T> T callMethod(Object object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, methodNames, parameterTypes, args);
    }

    default <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, new String[]{methodName}, parameterTypes, args);
    }

    default <T> T callMethod(Class<?> clazz, Object object, String... methodNames) {
        return callMethod(clazz, object, methodNames, new Class[]{});
    }

    default <T> T callMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(clazz, object, new String[]{methodName}, parameterTypes, args);
    }

    default <T> T callMethod(Class<?> clazz, Object object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        for (String name : methodNames) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return (T) method.invoke(object, args);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following methods in the class %s: %s", clazz.getSimpleName(), String.join(", ", methodNames)));
    }

    default <T> T callMethod(Class<?> object, String... methodNames) {
        return callMethod(object, methodNames, new Class[]{});
    }

    default <T> T callMethod(Class<?> object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object, null, methodNames, parameterTypes, args);
    }

    default <T> T callMethod(Class<?> object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object, null, new String[]{methodName}, parameterTypes, args);
    }

    default Method getMethod(Class<?> clazz, String... methodNames) {
        return getMethod(clazz, methodNames, new Class[]{});
    }

    default Method getMethod(Class<?> clazz, String[] methodNames, Class<?>[] parameterTypes) {
        for (String name : methodNames) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following methods in the class %s: %s", clazz.getSimpleName(), String.join(", ", methodNames)));
    }

    default <T> T call(Method method, @Nullable Object instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to call method %s", method.getName()));
        }
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

    default <T> T getField(Object object, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(object);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following fields in the class %s: %s", object.getClass().getSimpleName(), String.join(", ", fieldNames)));
    }

    default <T> T getField(Class<?> clazz, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(null);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following fields in the class %s: %s", clazz.getSimpleName(), String.join(", ", fieldNames)));
    }

    /**
     * @param classNames the class names including the package name
     * @return the class
     */
    default Class<?> getClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

    /**
     * @param classNames the class names including the package name, starting after the bukkit the base package e.g. <code>org.bukkit.craftbukkit.v1_20_R3.CraftServer</code> would be <code>CraftServer</code>
     * @return the class
     */
    default Class<?> getBukkitClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(String.format("%s.%s", getBaseBukkitPackage(), className));
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

    default Class<?> getServerClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(String.format("%s.%s", getBaseServerPackage(), className));
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

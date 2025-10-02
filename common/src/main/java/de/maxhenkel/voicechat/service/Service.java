package de.maxhenkel.voicechat.service;

import de.maxhenkel.voicechat.Voicechat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

public class Service {

    // We need a separate logger, since the voice chat logger itself needs the serviceloader
    private static final Logger LOGGER = LogManager.getLogger(Voicechat.MODID);

    public static <T> T get(Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass, serviceClass.getClassLoader()).findFirst().orElseGet(() -> {
            LOGGER.warn("Failed to load service {} with ServiceLoader", serviceClass.getSimpleName());
            try {
                return loadFallback(serviceClass);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load service %s".formatted(serviceClass.getSimpleName()), e);
            }
        });
    }

    private static <T> T loadFallback(Class<T> serviceClass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> fallbackClass = loadFallbackClass(serviceClass);
        if (!serviceClass.isAssignableFrom(fallbackClass)) {
            throw new ClassNotFoundException("Class %s is not an instance of %s".formatted(fallbackClass.getSimpleName(), serviceClass.getSimpleName()));
        }
        return (T) fallbackClass.getDeclaredConstructor().newInstance();
    }

    private static Class<?> loadFallbackClass(Class<?> serviceClass) throws ClassNotFoundException {
        Class<?> implClass = loadClassWithPrefix(serviceClass, "Fabric");
        if (implClass != null) {
            return implClass;
        }
        implClass = loadClassWithPrefix(serviceClass, "NeoForge");
        if (implClass != null) {
            return implClass;
        }
        implClass = loadClassWithPrefix(serviceClass, "Forge");
        if (implClass != null) {
            return implClass;
        }
        implClass = loadClassWithPrefix(serviceClass, "Quilt");
        if (implClass != null) {
            return implClass;
        }
        implClass = loadClassWithPrefix(serviceClass, "Paper");
        if (implClass != null) {
            return implClass;
        }
        throw new ClassNotFoundException("Implementation of %s not found in package %s".formatted(serviceClass.getSimpleName(), serviceClass.getPackageName()));
    }

    @Nullable
    private static Class<?> loadClassWithPrefix(Class<?> serviceClass, String prefix) {
        try {
            return Class.forName(serviceClass.getPackageName() + "." + prefix + serviceClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

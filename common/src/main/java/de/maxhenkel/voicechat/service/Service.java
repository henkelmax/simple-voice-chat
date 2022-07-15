package de.maxhenkel.voicechat.service;

import de.maxhenkel.voicechat.Voicechat;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

public class Service {

    public static <T> T get(Class<T> serviceClass) {
        Iterator<T> iterator = ServiceLoader.load(serviceClass).iterator();
        if (!iterator.hasNext()) {
            Voicechat.LOGGER.warn("Failed to load service {} with ServiceLoader", serviceClass.getSimpleName());
            try {
                return loadFallback(serviceClass);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load service %s".formatted(serviceClass.getSimpleName()), e);
            }
        }
        return iterator.next();
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
        implClass = loadClassWithPrefix(serviceClass, "Forge");
        if (implClass != null) {
            return implClass;
        }
        implClass = loadClassWithPrefix(serviceClass, "Quilt");
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

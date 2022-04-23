package de.maxhenkel.voicechat.service;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Service {

    public static <T> T get(Class<T> serviceClass) {
        Iterator<T> iterator = ServiceLoader.load(serviceClass).iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("Failed to load service '" + serviceClass.getSimpleName() + "'");
        }
        return iterator.next();
    }

}

package de.maxhenkel.voicechat.service;

import java.util.ServiceLoader;

public class Service {

    public static <T> T get(Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass).findFirst().orElseThrow(() -> new IllegalStateException("Failed to load service '%s'".formatted(serviceClass.getSimpleName())));
    }

}

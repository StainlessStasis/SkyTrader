package io.github.stainlessstasis.skytrader.platform;

import io.github.stainlessstasis.skytrader.ModConstants;

import java.util.ServiceLoader;


public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        ModConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
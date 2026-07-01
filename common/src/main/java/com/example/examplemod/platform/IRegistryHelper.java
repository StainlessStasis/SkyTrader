package com.example.examplemod.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IRegistryHelper {
    /**
     * Registers a platform-agnostic item.
     */
    <T extends Item> Supplier<T> registerItem(String name, Function<ResourceKey<Item>, T> factory);
}

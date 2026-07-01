package com.example.examplemod.platform;

import com.example.examplemod.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ModConstants.MOD_ID);

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<ResourceKey<Item>, T> factory) {
        Identifier id = ModConstants.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        return ITEMS.register(name, () -> factory.apply(itemKey));
    }
}

package io.github.stainlessstasis.skytrader.platform;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<ResourceKey<Item>, T> factory) {
        Identifier id = ModConstants.id(name);
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);

        T item = factory.apply(key);
        Registry.register(BuiltInRegistries.ITEM, id, item);

        return () -> item;
    }

    @Override
    public Supplier<GameRule<Boolean>> registerBooleanGameRule(String name, GameRuleCategory category, boolean defaultValue) {
        GameRule<Boolean> rule = GameRuleBuilder
                .forBoolean(defaultValue)
                .category(category)
                .buildAndRegister(ModConstants.id(name));
        return () -> rule;
    }
}

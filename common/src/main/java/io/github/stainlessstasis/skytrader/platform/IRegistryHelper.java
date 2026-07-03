package io.github.stainlessstasis.skytrader.platform;

import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import java.util.function.Function;
import java.util.function.Supplier;

public interface IRegistryHelper {
    /**
     * Registers a platform-agnostic item.
     */
    <T extends Item> Supplier<T> registerItem(String name, Function<ResourceKey<Item>, T> factory);
    /**
     * Registers a platform-agnostic game rule with a boolean value.
     */
    Supplier<GameRule<Boolean>> registerBooleanGameRule(String name, GameRuleCategory category, boolean defaultValue);
    /**
     * Registers a platform-agnostic advancement trigger with using the {@link net.minecraft.advancements.triggers.ImpossibleTrigger ImpossibleTrigger}.
     * These advancements are intended to be manually triggered.
     */
    Supplier<ImpossibleTrigger> registerAdvancementTrigger(String name);
}

package io.github.stainlessstasis.skytrader.platform;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.ModGameRules;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber
public class NeoForgeRegistryHelper implements IRegistryHelper {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ModConstants.MOD_ID);
    public static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, ModConstants.MOD_ID);
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(Registries.TRIGGER_TYPE, ModConstants.MOD_ID);

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<ResourceKey<Item>, T> factory) {
        Identifier id = ModConstants.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        return ITEMS.register(name, () -> factory.apply(itemKey));
    }

    @Override
    public Supplier<GameRule<Boolean>> registerBooleanGameRule(String name, GameRuleCategory category, boolean defaultValue) {
        Supplier<GameRule<?>> registered = GAME_RULES.register(name, () -> new GameRule<>(
                category, GameRuleType.BOOL, BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean, Codec.BOOL,
                b -> b ? 1 : 0, defaultValue, FeatureFlagSet.of()
        ));
        @SuppressWarnings("unchecked")
        Supplier<GameRule<Boolean>> typed = (Supplier<GameRule<Boolean>>) (Supplier<?>) registered;
        return typed;
    }

    @Override
    public Supplier<ImpossibleTrigger> registerAdvancementTrigger(String name) {
        return TRIGGER_TYPES.register(name, ImpossibleTrigger::new);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        GAME_RULES.register(bus);
        TRIGGER_TYPES.register(bus);
        ModItems.init();
        ModGameRules.init();
        ModAdvancements.init();
    }
}

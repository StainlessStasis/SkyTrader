package io.github.stainlessstasis.skytrader.trader;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkyTraderTrades {
    public static final RegistrySetBuilder REGISTRY_BUILDER = new RegistrySetBuilder()
            .add(Registries.VILLAGER_TRADE, SkyTraderTrades::bootstrapTrades)
            .add(Registries.TRADE_SET, SkyTraderTrades::bootstrapTradeSets);

    public static final ResourceKey<VillagerTrade> SELL_RIDE = resourceKey("buy_emerald_for_ride");
    // populated during bootstrapTrades, consumed during bootstrapTradeSets
    private static List<ResourceKey<VillagerTrade>> harnessTradeKeys;
    private static List<ResourceKey<VillagerTrade>> boatTradeKeys;

    // sky utility
    public static final ResourceKey<VillagerTrade> SELL_SLOW_FALLING_POTION = resourceKey("sell_slow_falling_potion");
    public static final ResourceKey<VillagerTrade> SELL_FEATHER_FALLING_BOOK = resourceKey("sell_feather_falling_book");
    public static final ResourceKey<VillagerTrade> SELL_WIND_CHARGE = resourceKey("sell_wind_charge");
    public static final ResourceKey<VillagerTrade> SELL_FIREWORK_ROCKET = resourceKey("sell_firework_rocket");
    public static final ResourceKey<VillagerTrade> SELL_PHANTOM_MEMBRANE = resourceKey("sell_phantom_membrane");
    public static final ResourceKey<VillagerTrade> SELL_SNOWBALL = resourceKey("sell_snowball");
    public static final ResourceKey<VillagerTrade> SELL_JUMP_STEW = resourceKey("sell_jump_stew");
    public static final ResourceKey<VillagerTrade> SELL_SLOW_FALL_STEW = resourceKey("sell_slow_fall_stew");
    public static final ResourceKey<VillagerTrade> SELL_SCAFFOLDING = resourceKey("sell_scaffolding");

    // mount utility
    public static final ResourceKey<VillagerTrade> SELL_SADDLE = resourceKey("sell_saddle");
    public static final ResourceKey<VillagerTrade> SELL_LEAD = resourceKey("sell_lead");
    public static final ResourceKey<VillagerTrade> SELL_NAME_TAG = resourceKey("sell_name_tag");
    public static final ResourceKey<VillagerTrade> SELL_HAY_BLOCK = resourceKey("sell_hay_block");
    public static final ResourceKey<VillagerTrade> SELL_IRON_HORSE_ARMOR = resourceKey("sell_iron_horse_armor");

    // ghast/nether themed
    public static final ResourceKey<VillagerTrade> SELL_GHAST_TEAR = resourceKey("sell_ghast_tear");
    public static final ResourceKey<VillagerTrade> SELL_FIRE_CHARGE = resourceKey("sell_fire_charge");
    public static final ResourceKey<VillagerTrade> SELL_MAGMA_CREAM = resourceKey("sell_magma_cream");
    public static final ResourceKey<VillagerTrade> SELL_SLIME_BALL = resourceKey("sell_slime_ball");
    public static final ResourceKey<VillagerTrade> SELL_NETHER_WART = resourceKey("sell_nether_wart");
    public static final ResourceKey<VillagerTrade> SELL_GLOWSTONE_DUST = resourceKey("sell_glowstone_dust");
    public static final ResourceKey<VillagerTrade> SELL_SOUL_SAND = resourceKey("sell_soul_sand");
    public static final ResourceKey<VillagerTrade> SELL_NETHER_BRICKS = resourceKey("sell_nether_bricks");
    public static final ResourceKey<VillagerTrade> SELL_CRIMSON_FUNGUS = resourceKey("sell_crimson_fungus");

    // rare
    public static final ResourceKey<VillagerTrade> SELL_ENDER_PEARL = resourceKey("sell_ender_pearl");
    public static final ResourceKey<VillagerTrade> SELL_NAUTILUS_SHELL = resourceKey("sell_nautilus_shell");
    public static final ResourceKey<VillagerTrade> SELL_HEART_OF_THE_SEA = resourceKey("sell_heart_of_the_sea");
    public static final ResourceKey<VillagerTrade> SELL_CHORUS_FRUIT = resourceKey("sell_chorus_fruit");
    public static final ResourceKey<VillagerTrade> SELL_BLUE_ICE = resourceKey("sell_blue_ice");
    public static final ResourceKey<VillagerTrade> SELL_HONEYCOMB = resourceKey("sell_honeycomb");

    public static final ResourceKey<TradeSet> SKY_TRADER_RIDE = tradeSetKey("sky_trader/ride");
    public static final ResourceKey<TradeSet> SKY_TRADER_HARNESS = tradeSetKey("sky_trader/harness");
    public static final ResourceKey<TradeSet> SKY_TRADER_BOAT = tradeSetKey("sky_trader/boat");
    public static final ResourceKey<TradeSet> SKY_TRADER_SKY_UTILITY = tradeSetKey("sky_trader/sky_utility");
    public static final ResourceKey<TradeSet> SKY_TRADER_MOUNT_UTILITY = tradeSetKey("sky_trader/mount_utility");
    public static final ResourceKey<TradeSet> SKY_TRADER_COMMON = tradeSetKey("sky_trader/common");
    public static final ResourceKey<TradeSet> SKY_TRADER_RARE = tradeSetKey("sky_trader/rare");

    public static ResourceKey<VillagerTrade> resourceKey(String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, ModConstants.id(path));
    }

    public static ResourceKey<TradeSet> tradeSetKey(String path) {
        return ResourceKey.create(Registries.TRADE_SET, ModConstants.id(path));
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        Holder<Enchantment> featherFalling = enchantments.getOrThrow(Enchantments.FEATHER_FALLING);

        // ride payment - always offered
        context.register(SELL_RIDE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 12),
                new ItemStackTemplate(ModItems.SKYFARE_TICKET.get()),
                16, 10, 0.05f, Optional.empty(), List.of()));

        harnessTradeKeys = registerHarnessTrades(context);
        boatTradeKeys = registerBoatTrades(context);

        // sky utility
        context.register(SELL_SLOW_FALLING_POTION, new VillagerTrade(
                new TradeCost(Items.EMERALD, 8), new ItemStackTemplate(Items.POTION),
                8, 10, 0.05f, Optional.empty(),
                List.of(SetPotionFunction.setPotion(Potions.SLOW_FALLING).build())));
        context.register(SELL_FEATHER_FALLING_BOOK, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(28, 36)), new ItemStackTemplate(Items.ENCHANTED_BOOK),
                3, 30, 0.2f, Optional.empty(),
                VillagerTrades.enchantedBook(items, featherFalling, 3)));
        context.register(SELL_WIND_CHARGE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.WIND_CHARGE, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_FIREWORK_ROCKET, new VillagerTrade(
                new TradeCost(Items.EMERALD, 6), new ItemStackTemplate(Items.FIREWORK_ROCKET, 4),
                12, 7, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_PHANTOM_MEMBRANE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.PHANTOM_MEMBRANE, 2),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_SNOWBALL, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(Items.SNOWBALL, 8),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_JUMP_STEW, new VillagerTrade(
                new TradeCost(Items.EMERALD, 4), new ItemStackTemplate(Items.SUSPICIOUS_STEW, 1),
                12, 5, 0.05f, Optional.empty(),
                List.of(SetStewEffectFunction.stewEffect()
                        .withEffect(MobEffects.JUMP_BOOST, UniformGenerator.between(5, 8))
                        .build())
        ));
        context.register(SELL_SLOW_FALL_STEW, new VillagerTrade(
                new TradeCost(Items.EMERALD, 4), new ItemStackTemplate(Items.SUSPICIOUS_STEW, 1),
                12, 5, 0.05f, Optional.empty(),
                List.of(SetStewEffectFunction.stewEffect()
                        .withEffect(MobEffects.SLOW_FALLING, UniformGenerator.between(5, 8))
                        .build())
        ));
        context.register(SELL_SCAFFOLDING, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.SCAFFOLDING, 16),
                12, 1, 0.05f, Optional.empty(), List.of()));

        // mount utility
        context.register(SELL_SADDLE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5), new ItemStackTemplate(Items.SADDLE),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_LEAD, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.LEAD, 1),
                12, 3, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NAME_TAG, new VillagerTrade(
                new TradeCost(Items.EMERALD, 4), new ItemStackTemplate(Items.NAME_TAG),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_HAY_BLOCK, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(3, 5)), new ItemStackTemplate(Items.HAY_BLOCK, 2),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_IRON_HORSE_ARMOR, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(7, 9)), new ItemStackTemplate(Items.IRON_HORSE_ARMOR),
                4, 10, 0.05f, Optional.empty(), List.of()));

        // ghast/nether themed
        context.register(SELL_GHAST_TEAR, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(3, 5)), new ItemStackTemplate(Items.GHAST_TEAR, 1),
                12, 7, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_FIRE_CHARGE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.FIRE_CHARGE, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_MAGMA_CREAM, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.MAGMA_CREAM, 3),
                12, 7, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_SLIME_BALL, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.SLIME_BALL, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NETHER_WART, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.NETHER_WART, 3),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_GLOWSTONE_DUST, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(Items.GLOWSTONE_DUST, 4),
                12, 3, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_SOUL_SAND, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(Items.SOUL_SAND, 4),
                12, 3, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NETHER_BRICKS, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.NETHER_BRICKS, 8),
                12, 3, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_CRIMSON_FUNGUS, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(Items.CRIMSON_FUNGUS, 2),
                8, 3, 0.05f, Optional.empty(), List.of()));

        // rare
        context.register(SELL_ENDER_PEARL, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(5, 7)), new ItemStackTemplate(Items.ENDER_PEARL, 2),
                6, 20, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NAUTILUS_SHELL, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(5, 7)), new ItemStackTemplate(Items.NAUTILUS_SHELL),
                6, 25, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_HEART_OF_THE_SEA, new VillagerTrade(
                new TradeCost(Items.EMERALD, UniformGenerator.between(9, 11)), new ItemStackTemplate(Items.HEART_OF_THE_SEA),
                1, 30, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_CHORUS_FRUIT, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.CHORUS_FRUIT, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_BLUE_ICE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.BLUE_ICE, 4),
                8, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_HONEYCOMB, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.HONEYCOMB, 3),
                12, 5, 0.05f, Optional.empty(), List.of()));
    }

    private static List<ResourceKey<VillagerTrade>> registerHarnessTrades(BootstrapContext<VillagerTrade> context) {
        return List.of(
                registerHarness(context, "sell_white_harness", Items.WHITE_HARNESS),
                registerHarness(context, "sell_orange_harness", Items.ORANGE_HARNESS),
                registerHarness(context, "sell_magenta_harness", Items.MAGENTA_HARNESS),
                registerHarness(context, "sell_light_blue_harness", Items.LIGHT_BLUE_HARNESS),
                registerHarness(context, "sell_yellow_harness", Items.YELLOW_HARNESS),
                registerHarness(context, "sell_lime_harness", Items.LIME_HARNESS),
                registerHarness(context, "sell_pink_harness", Items.PINK_HARNESS),
                registerHarness(context, "sell_gray_harness", Items.GRAY_HARNESS),
                registerHarness(context, "sell_light_gray_harness", Items.LIGHT_GRAY_HARNESS),
                registerHarness(context, "sell_cyan_harness", Items.CYAN_HARNESS),
                registerHarness(context, "sell_purple_harness", Items.PURPLE_HARNESS),
                registerHarness(context, "sell_blue_harness", Items.BLUE_HARNESS),
                registerHarness(context, "sell_brown_harness", Items.BROWN_HARNESS),
                registerHarness(context, "sell_green_harness", Items.GREEN_HARNESS),
                registerHarness(context, "sell_red_harness", Items.RED_HARNESS),
                registerHarness(context, "sell_black_harness", Items.BLACK_HARNESS)
        );
    }

    private static ResourceKey<VillagerTrade> registerHarness(BootstrapContext<VillagerTrade> context, String path, Item harness) {
        ResourceKey<VillagerTrade> key = resourceKey(path);
        context.register(key, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5), new ItemStackTemplate(harness),
                8, 5, 0.05f, Optional.empty(), List.of()));
        return key;
    }

    private static List<ResourceKey<VillagerTrade>> registerBoatTrades(BootstrapContext<VillagerTrade> context) {
        return List.of(
                registerBoat(context, "sell_oak_boat", Items.OAK_BOAT),
                registerBoat(context, "sell_spruce_boat", Items.SPRUCE_BOAT),
                registerBoat(context, "sell_birch_boat", Items.BIRCH_BOAT),
                registerBoat(context, "sell_jungle_boat", Items.JUNGLE_BOAT),
                registerBoat(context, "sell_acacia_boat", Items.ACACIA_BOAT),
                registerBoat(context, "sell_dark_oak_boat", Items.DARK_OAK_BOAT),
                registerBoat(context, "sell_mangrove_boat", Items.MANGROVE_BOAT),
                registerBoat(context, "sell_cherry_boat", Items.CHERRY_BOAT),
                registerBoat(context, "sell_pale_oak_boat", Items.PALE_OAK_BOAT),
                registerBoat(context, "sell_bamboo_raft", Items.BAMBOO_RAFT)
        );
    }

    private static ResourceKey<VillagerTrade> registerBoat(BootstrapContext<VillagerTrade> context, String path, Item boat) {
        ResourceKey<VillagerTrade> key = resourceKey(path);
        context.register(key, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5), new ItemStackTemplate(boat),
                8, 5, 0.05f, Optional.empty(), List.of()));
        return key;
    }

    public static void bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        HolderGetter<VillagerTrade> trades = context.lookup(Registries.VILLAGER_TRADE);

        context.register(SKY_TRADER_RIDE, new TradeSet(
                HolderSet.direct(trades.getOrThrow(SELL_RIDE)),
                ConstantValue.exactly(1),
                false,
                Optional.empty()
        ));

        List<Holder<VillagerTrade>> harnessHolders = harnessTradeKeys.stream()
                .map(trades::getOrThrow)
                .collect(Collectors.toUnmodifiableList());
        context.register(SKY_TRADER_HARNESS, new TradeSet(
                HolderSet.direct(harnessHolders),
                ConstantValue.exactly(1),
                false,
                Optional.empty()
        ));

        List<Holder<VillagerTrade>> boatHolders = boatTradeKeys.stream()
                .map(trades::getOrThrow)
                .collect(Collectors.toUnmodifiableList());
        context.register(SKY_TRADER_BOAT, new TradeSet(
                HolderSet.direct(boatHolders),
                ConstantValue.exactly(1),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_SKY_UTILITY, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_SLOW_FALLING_POTION),
                        trades.getOrThrow(SELL_FEATHER_FALLING_BOOK),
                        trades.getOrThrow(SELL_WIND_CHARGE),
                        trades.getOrThrow(SELL_FIREWORK_ROCKET),
                        trades.getOrThrow(SELL_PHANTOM_MEMBRANE),
                        trades.getOrThrow(SELL_SNOWBALL),
                        trades.getOrThrow(SELL_JUMP_STEW),
                        trades.getOrThrow(SELL_SLOW_FALL_STEW),
                        trades.getOrThrow(SELL_SCAFFOLDING)
                ),
                ConstantValue.exactly(3),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_MOUNT_UTILITY, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_SADDLE),
                        trades.getOrThrow(SELL_LEAD),
                        trades.getOrThrow(SELL_NAME_TAG),
                        trades.getOrThrow(SELL_HAY_BLOCK),
                        trades.getOrThrow(SELL_IRON_HORSE_ARMOR)
                ),
                ConstantValue.exactly(2),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_COMMON, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_GHAST_TEAR),
                        trades.getOrThrow(SELL_FIRE_CHARGE),
                        trades.getOrThrow(SELL_MAGMA_CREAM),
                        trades.getOrThrow(SELL_SLIME_BALL),
                        trades.getOrThrow(SELL_NETHER_WART),
                        trades.getOrThrow(SELL_GLOWSTONE_DUST),
                        trades.getOrThrow(SELL_SOUL_SAND),
                        trades.getOrThrow(SELL_NETHER_BRICKS),
                        trades.getOrThrow(SELL_CRIMSON_FUNGUS)
                ),
                UniformGenerator.between(2, 3),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_RARE, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_ENDER_PEARL),
                        trades.getOrThrow(SELL_NAUTILUS_SHELL),
                        trades.getOrThrow(SELL_HEART_OF_THE_SEA),
                        trades.getOrThrow(SELL_CHORUS_FRUIT),
                        trades.getOrThrow(SELL_BLUE_ICE),
                        trades.getOrThrow(SELL_HONEYCOMB)
                ),
                UniformGenerator.between(1, 2),
                false,
                Optional.empty()
        ));
    }
}
package com.example.examplemod.trader;

import com.example.examplemod.ModConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Optional;

public class SkyTraderTrades {
    public static final RegistrySetBuilder REGISTRY_BUILDER = new RegistrySetBuilder()
            .add(Registries.VILLAGER_TRADE, SkyTraderTrades::bootstrapTrades)
            .add(Registries.TRADE_SET, SkyTraderTrades::bootstrapTradeSets);

    public static final ResourceKey<VillagerTrade> SELL_GHAST_HARNESS = resourceKey("sell_ghast_harness");
    public static final ResourceKey<VillagerTrade> BUY_EMERALD_FOR_RIDE = resourceKey("buy_emerald_for_ride");

    public static final ResourceKey<VillagerTrade> SELL_SADDLE = resourceKey("sell_saddle");
    public static final ResourceKey<VillagerTrade> SELL_LEAD = resourceKey("sell_lead");
    public static final ResourceKey<VillagerTrade> SELL_FIREWORK_ROCKET = resourceKey("sell_firework_rocket");
    public static final ResourceKey<VillagerTrade> SELL_NAME_TAG = resourceKey("sell_name_tag");

    public static final ResourceKey<VillagerTrade> SELL_GHAST_TEAR = resourceKey("sell_ghast_tear");
    public static final ResourceKey<VillagerTrade> SELL_FIRE_CHARGE = resourceKey("sell_fire_charge");
    public static final ResourceKey<VillagerTrade> SELL_BLAZE_ROD = resourceKey("sell_blaze_rod");
    public static final ResourceKey<VillagerTrade> SELL_MAGMA_CREAM = resourceKey("sell_magma_cream");
    public static final ResourceKey<VillagerTrade> SELL_SLIME_BALL = resourceKey("sell_slime_ball");

    public static final ResourceKey<VillagerTrade> SELL_ENDER_PEARL = resourceKey("sell_ender_pearl");
    public static final ResourceKey<VillagerTrade> SELL_NAUTILUS_SHELL = resourceKey("sell_nautilus_shell");

    // always present as the first 2 trades
    public static final ResourceKey<TradeSet> SKY_TRADER_RIDE = tradeSetKey("sky_trader/ride");
    public static final ResourceKey<TradeSet> SKY_TRADER_HARNESS = tradeSetKey("sky_trader/harness");

    // randomly chosen
    public static final ResourceKey<TradeSet> SKY_TRADER_UTILITY = tradeSetKey("sky_trader/utility");
    public static final ResourceKey<TradeSet> SKY_TRADER_COMMON = tradeSetKey("sky_trader/common");
    public static final ResourceKey<TradeSet> SKY_TRADER_RARE = tradeSetKey("sky_trader/rare");

    public static ResourceKey<VillagerTrade> resourceKey(String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, ModConstants.id(path));
    }

    public static ResourceKey<TradeSet> tradeSetKey(String path) {
        return ResourceKey.create(Registries.TRADE_SET, ModConstants.id(path));
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        // ride payment
        context.register(BUY_EMERALD_FOR_RIDE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1),
                new ItemStackTemplate(Items.SADDLE), // TODO: placeholder
                99, 1, 0, Optional.empty(), List.of()));

        // always offered
        context.register(SELL_GHAST_HARNESS, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5), new ItemStackTemplate(Items.HARNESS.white()),
                8, 5, 0.05f, Optional.empty(), List.of()));

        // utilities
        context.register(SELL_SADDLE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 6), new ItemStackTemplate(Items.SADDLE),
                8, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_LEAD, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.LEAD, 2),
                12, 1, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_FIREWORK_ROCKET, new VillagerTrade(
                new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(Items.FIREWORK_ROCKET, 8),
                12, 1, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NAME_TAG, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.NAME_TAG),
                12, 5, 0.05f, Optional.empty(), List.of()));

        // ghast/nether themed
        context.register(SELL_GHAST_TEAR, new VillagerTrade(
                new TradeCost(Items.EMERALD, 4), new ItemStackTemplate(Items.GHAST_TEAR, 2),
                12, 10, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_FIRE_CHARGE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.FIRE_CHARGE, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_BLAZE_ROD, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.BLAZE_ROD, 2),
                12, 15, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_MAGMA_CREAM, new VillagerTrade(
                new TradeCost(Items.EMERALD, 3), new ItemStackTemplate(Items.MAGMA_CREAM, 3),
                12, 10, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_SLIME_BALL, new VillagerTrade(
                new TradeCost(Items.EMERALD, 2), new ItemStackTemplate(Items.SLIME_BALL, 4),
                12, 5, 0.05f, Optional.empty(), List.of()));

        // rare
        context.register(SELL_ENDER_PEARL, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5), new ItemStackTemplate(Items.ENDER_PEARL, 2),
                8, 20, 0.05f, Optional.empty(), List.of()));
        context.register(SELL_NAUTILUS_SHELL, new VillagerTrade(
                new TradeCost(Items.EMERALD, 6), new ItemStackTemplate(Items.NAUTILUS_SHELL),
                6, 25, 0.05f, Optional.empty(), List.of()));
    }

    public static void bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        HolderGetter<VillagerTrade> trades = context.lookup(Registries.VILLAGER_TRADE);

        context.register(SKY_TRADER_RIDE, new TradeSet(
                HolderSet.direct(trades.getOrThrow(BUY_EMERALD_FOR_RIDE)),
                ConstantValue.exactly(1),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_HARNESS, new TradeSet(
                HolderSet.direct(trades.getOrThrow(SELL_GHAST_HARNESS)),
                ConstantValue.exactly(1),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_UTILITY, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_SADDLE),
                        trades.getOrThrow(SELL_LEAD),
                        trades.getOrThrow(SELL_FIREWORK_ROCKET),
                        trades.getOrThrow(SELL_NAME_TAG)
                ),
                ConstantValue.exactly(2),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_COMMON, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_GHAST_TEAR),
                        trades.getOrThrow(SELL_FIRE_CHARGE),
                        trades.getOrThrow(SELL_BLAZE_ROD),
                        trades.getOrThrow(SELL_MAGMA_CREAM),
                        trades.getOrThrow(SELL_SLIME_BALL)
                ),
                UniformGenerator.between(2, 4),
                false,
                Optional.empty()
        ));

        context.register(SKY_TRADER_RARE, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_ENDER_PEARL),
                        trades.getOrThrow(SELL_NAUTILUS_SHELL)
                ),
                UniformGenerator.between(0, 1),
                false,
                Optional.empty()
        ));
    }
}
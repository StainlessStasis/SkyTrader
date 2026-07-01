package com.example.examplemod.trader;

import com.example.examplemod.ModConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;

public class SkyTraderTrades {
    public static final ResourceKey<VillagerTrade> SELL_GHAST_HARNESS =
            resourceKey("sell_ghast_harness");
    public static final ResourceKey<VillagerTrade> BUY_EMERALD_FOR_RIDE =
            resourceKey("buy_emerald_for_ride");
    public static final ResourceKey<TradeSet> SKY_TRADER_COMMON =
            tradeSetKey("sky_trader/common");

    public static ResourceKey<VillagerTrade> resourceKey(String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, ModConstants.id(path));
    }

    public static ResourceKey<TradeSet> tradeSetKey(String path) {
        return ResourceKey.create(Registries.TRADE_SET, ModConstants.id(path));
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        context.register(SELL_GHAST_HARNESS, new VillagerTrade(
                new TradeCost(Items.EMERALD, 5),
                new ItemStackTemplate(Items.HARNESS.white()),
                3, 0, 0, Optional.empty(), List.of()));


        context.register(BUY_EMERALD_FOR_RIDE, new VillagerTrade(
                new TradeCost(Items.EMERALD, 12),
                new ItemStackTemplate(Items.SADDLE), // TODO: replace placeholder
                3, 0, 0, Optional.empty(), List.of()));
    }

    public static void bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        HolderGetter<VillagerTrade> trades = context.lookup(Registries.VILLAGER_TRADE);
        context.register(SKY_TRADER_COMMON, new TradeSet(
                HolderSet.direct(
                        trades.getOrThrow(SELL_GHAST_HARNESS),
                        trades.getOrThrow(BUY_EMERALD_FOR_RIDE)
                ),
                ConstantValue.exactly(3),
                false,
                Optional.empty()
        ));
    }
}
package com.example.examplemod;

import com.example.examplemod.trader.SkyTraderTrades;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class ModRegistries {
    public static final RegistrySetBuilder SKY_TRADER_TRADES_BUILDER = new RegistrySetBuilder()
            .add(Registries.VILLAGER_TRADE, SkyTraderTrades::bootstrapTrades)
            .add(Registries.TRADE_SET, SkyTraderTrades::bootstrapTradeSets);
}

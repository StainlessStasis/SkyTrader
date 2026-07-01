package io.github.stainlessstasis.skytrader.entity;

import io.github.stainlessstasis.skytrader.trader.SkyTraderTrades;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class SkyTrader extends WanderingTrader {
    public SkyTrader(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }

    @Override
    protected void updateTrades(@NonNull ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_RIDE);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_HARNESS);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_UTILITY);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_COMMON);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_RARE);
    }
}
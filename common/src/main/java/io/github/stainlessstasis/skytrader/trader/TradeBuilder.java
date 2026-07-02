package io.github.stainlessstasis.skytrader.trader;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.*;

public class TradeBuilder {
    public static final Map<ResourceKey<TradeSet>, List<ResourceKey<VillagerTrade>>> CATEGORIZED_TRADES = new LinkedHashMap<>();

    private final String id;
    private final ResourceKey<VillagerTrade> key;
    private final Item sellItem;
    private int sellCount = 1;
    private NumberProvider emeraldCost;
    private int maxUses = 12;
    private int xp = -1; // calculate based on the emerald cost if not defined later on
    private float reputationDiscount = 0.05f;
    private final List<LootItemFunction> functions = new ArrayList<>();

    public TradeBuilder(String id, Item sellItem) {
        this.id = id;
        this.key = ResourceKey.create(Registries.VILLAGER_TRADE, ModConstants.id(id));
        this.sellItem = sellItem;
    }

    public static TradeBuilder sell(String id, Item item) {
        return new TradeBuilder(id, item);
    }

    public TradeBuilder count(int count) {
        this.sellCount = count;
        return this;
    }

    public static NumberProvider fixedPrice(int exact) {
        return UniformGenerator.between(exact, exact);
    }

    public TradeBuilder price(int min, int max) {
        this.emeraldCost = UniformGenerator.between(min, max);
        return this;
    }

    public TradeBuilder price(int flatPrice) {
        this.emeraldCost = fixedPrice(flatPrice);
        return this;
    }

    public TradeBuilder maxUses(int maxUses) {
        this.maxUses = maxUses;
        return this;
    }

    public TradeBuilder xp(int xp) {
        this.xp = xp;
        return this;
    }

    public TradeBuilder withFunction(LootItemFunction function) {
        this.functions.add(function);
        return this;
    }

    public ResourceKey<VillagerTrade> build(ResourceKey<TradeSet> tradeSet, BootstrapContext<VillagerTrade> context) {
        int xp = this.xp;
        if (xp == -1) {
            float cost = 5f;
            if (emeraldCost instanceof UniformGenerator uniform) {
                if (uniform.min() instanceof ConstantValue(float value)) cost = value;
            } else if (emeraldCost instanceof ConstantValue(float value)) {
                cost = value;
            }

            xp = cost > 8 ? 15 : cost > 4 ? 7 : 3;
        }

        VillagerTrade trade = new VillagerTrade(
                new TradeCost(Items.EMERALD, this.emeraldCost),
                new ItemStackTemplate(this.sellItem, this.sellCount),
                this.maxUses, xp, this.reputationDiscount, Optional.empty(), this.functions
        );

        context.register(this.key, trade);

        CATEGORIZED_TRADES.computeIfAbsent(tradeSet, _ -> new ArrayList<>()).add(this.key);
        return this.key;
    }
}
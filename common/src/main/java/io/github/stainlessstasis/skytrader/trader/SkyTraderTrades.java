package io.github.stainlessstasis.skytrader.trader;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkyTraderTrades {
    public static final RegistrySetBuilder REGISTRY_BUILDER = new RegistrySetBuilder()
            .add(Registries.VILLAGER_TRADE, SkyTraderTrades::bootstrapTrades)
            .add(Registries.TRADE_SET, SkyTraderTrades::bootstrapTradeSets);

    public static final ResourceKey<TradeSet> SKY_TRADER_RIDE = tradeSetKey("sky_trader/ride");
    public static final ResourceKey<TradeSet> SKY_TRADER_SKYFLARE = tradeSetKey("sky_trader/skyflare");
    public static final ResourceKey<TradeSet> SKY_TRADER_HARNESS = tradeSetKey("sky_trader/harness");
    public static final ResourceKey<TradeSet> SKY_TRADER_BOAT = tradeSetKey("sky_trader/boat");
    public static final ResourceKey<TradeSet> SKY_TRADER_SKY_UTILITY = tradeSetKey("sky_trader/sky_utility");
    public static final ResourceKey<TradeSet> SKY_TRADER_MOUNT_UTILITY = tradeSetKey("sky_trader/mount_utility");
    public static final ResourceKey<TradeSet> SKY_TRADER_COMMON = tradeSetKey("sky_trader/common");
    public static final ResourceKey<TradeSet> SKY_TRADER_RARE = tradeSetKey("sky_trader/rare");
    public static final ResourceKey<TradeSet> SKY_TRADER_SNACKS = tradeSetKey("sky_trader/snacks");

    public static ResourceKey<TradeSet> tradeSetKey(String path) {
        return ResourceKey.create(Registries.TRADE_SET, ModConstants.id(path));
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        // always present
        TradeBuilder.sell("sell_skyfare_ticket", ModItems.SKYFARE_TICKET.get()).price(12).maxUses(16).xp(10).build(SKY_TRADER_RIDE, context);
        TradeBuilder.sell("sell_skyflare", ModItems.SKYFLARE.get()).price(10).maxUses(4).xp(10).build(SKY_TRADER_SKYFLARE, context);
        registerHarnesses(context);
        registerBoats(context);

        // sky utility
        TradeBuilder.sell("sell_slow_falling_potion", Items.POTION).price(8).maxUses(8)
                .withFunction(SetPotionFunction.setPotion(Potions.SLOW_FALLING).build()).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_wind_charge", Items.WIND_CHARGE).count(4).price(2).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_firework_rocket", Items.FIREWORK_ROCKET).count(4).price(6).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_phantom_membrane", Items.PHANTOM_MEMBRANE).count(2).price(3).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_snowball", Items.SNOWBALL).count(8).price(1).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_scaffolding", Items.SCAFFOLDING).count(16).price(2).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_feather", Items.FEATHER).count(3).price(1).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_water_bucket", Items.WATER_BUCKET).price(8).build(SKY_TRADER_SKY_UTILITY, context);

        TradeBuilder.sell("sell_jump_stew", Items.SUSPICIOUS_STEW).price(4)
                .withFunction(SetStewEffectFunction.stewEffect().withEffect(MobEffects.JUMP_BOOST, UniformGenerator.between(5, 8)).build()).build(SKY_TRADER_SKY_UTILITY, context);
        TradeBuilder.sell("sell_slow_fall_stew", Items.SUSPICIOUS_STEW).price(4)
                .withFunction(SetStewEffectFunction.stewEffect().withEffect(MobEffects.SLOW_FALLING, UniformGenerator.between(5, 8)).build()).build(SKY_TRADER_SKY_UTILITY, context);

        // mount utility
        TradeBuilder.sell("sell_saddle", Items.SADDLE).price(5).build(SKY_TRADER_MOUNT_UTILITY, context);
        TradeBuilder.sell("sell_lead", Items.LEAD).price(2).build(SKY_TRADER_MOUNT_UTILITY, context);
        TradeBuilder.sell("sell_name_tag", Items.NAME_TAG).price(4).build(SKY_TRADER_MOUNT_UTILITY, context);
        TradeBuilder.sell("sell_hay_block", Items.HAY_BLOCK).count(2).price(3, 5).build(SKY_TRADER_MOUNT_UTILITY, context);
        TradeBuilder.sell("sell_iron_horse_armor", Items.IRON_HORSE_ARMOR).price(7, 9).maxUses(4).build(SKY_TRADER_MOUNT_UTILITY, context);

        // ghast/nether themed (common)
        TradeBuilder.sell("sell_ghast_tear", Items.GHAST_TEAR).price(3, 5).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_fire_charge", Items.FIRE_CHARGE).count(4).price(2).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_magma_cream", Items.MAGMA_CREAM).count(3).price(3).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_slime_ball", Items.SLIME_BALL).count(4).price(2).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_nether_wart", Items.NETHER_WART).count(3).price(2).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_glowstone_dust", Items.GLOWSTONE_DUST).count(4).price(1).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_soul_sand", Items.SOUL_SAND).count(4).price(1).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_nether_bricks", Items.NETHER_BRICKS).count(8).price(2).build(SKY_TRADER_COMMON, context);
        TradeBuilder.sell("sell_crimson_fungus", Items.CRIMSON_FUNGUS).count(2).price(1).maxUses(8).build(SKY_TRADER_COMMON, context);

        // rare
        TradeBuilder.sell("sell_ender_pearl", Items.ENDER_PEARL).count(2).price(5, 7).maxUses(6).build(SKY_TRADER_RARE, context);
        TradeBuilder.sell("sell_nautilus_shell", Items.NAUTILUS_SHELL).price(5, 7).maxUses(6).build(SKY_TRADER_RARE, context);
        TradeBuilder.sell("sell_heart_of_the_sea", Items.HEART_OF_THE_SEA).price(9, 11).maxUses(1).build(SKY_TRADER_RARE, context);
        TradeBuilder.sell("sell_chorus_fruit", Items.CHORUS_FRUIT).count(4).price(2).build(SKY_TRADER_RARE, context);
        TradeBuilder.sell("sell_blue_ice", Items.BLUE_ICE).count(4).price(3).maxUses(8).build(SKY_TRADER_RARE, context);
        TradeBuilder.sell("sell_honeycomb", Items.HONEYCOMB).count(3).price(2).build(SKY_TRADER_RARE, context);

        // mid-flight snacks
        TradeBuilder.sell("sell_bread", Items.BREAD).price(1).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_cookie", Items.COOKIE).count(4).price(1).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_apple", Items.APPLE).count(2).price(1).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_melon_slice", Items.MELON_SLICE).count(3).price(1).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_honey_bottle", Items.HONEY_BOTTLE).price(2).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_milk_bucket", Items.MILK_BUCKET).price(5).maxUses(6).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_pumpkin_pie", Items.PUMPKIN_PIE).price(2).build(SKY_TRADER_SNACKS, context);
        TradeBuilder.sell("sell_cooked_chicken", Items.COOKED_CHICKEN).price(3).build(SKY_TRADER_SNACKS, context);
    }

    private static void registerHarnesses(BootstrapContext<VillagerTrade> context) {
        List.of(
                Items.WHITE_HARNESS, Items.ORANGE_HARNESS, Items.MAGENTA_HARNESS, Items.LIGHT_BLUE_HARNESS,
                Items.YELLOW_HARNESS, Items.LIME_HARNESS, Items.PINK_HARNESS, Items.GRAY_HARNESS,
                Items.LIGHT_GRAY_HARNESS, Items.CYAN_HARNESS, Items.PURPLE_HARNESS, Items.BLUE_HARNESS,
                Items.BROWN_HARNESS, Items.GREEN_HARNESS, Items.RED_HARNESS, Items.BLACK_HARNESS
        ).forEach(harness -> {
            String colorName = BuiltInRegistries.ITEM.getKey(harness).getPath();

            TradeBuilder.sell("sell_" + colorName, harness)
                    .price(5)
                    .maxUses(8)
                    .build(SKY_TRADER_HARNESS, context);
        });
    }

    private static void registerBoats(BootstrapContext<VillagerTrade> context) {
        List.of(Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT, Items.ACACIA_BOAT,
                        Items.DARK_OAK_BOAT, Items.MANGROVE_BOAT, Items.CHERRY_BOAT, Items.PALE_OAK_BOAT, Items.BAMBOO_RAFT)
                .forEach(boat -> {
                    String pathName = BuiltInRegistries.ITEM.getKey(boat).getPath();
                    TradeBuilder.sell("sell_" + pathName, boat).price(5).maxUses(8).build(SKY_TRADER_BOAT, context);
                });
    }

    public static void bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        HolderGetter<VillagerTrade> tradesLookup = context.lookup(Registries.VILLAGER_TRADE);

        TradeBuilder.CATEGORIZED_TRADES.forEach((tradeSetKey, tradeKeys) -> {
            List<Holder<VillagerTrade>> holders = tradeKeys.stream()
                    .map(tradesLookup::getOrThrow)
                    .collect(Collectors.toUnmodifiableList());

            NumberProvider rolls = ConstantValue.exactly(1);
            if (tradeSetKey.equals(SKY_TRADER_SKY_UTILITY)) rolls = ConstantValue.exactly(3);
            else if (tradeSetKey.equals(SKY_TRADER_MOUNT_UTILITY)) rolls = ConstantValue.exactly(2);
            else if (tradeSetKey.equals(SKY_TRADER_COMMON)) rolls = UniformGenerator.between(2, 3);
            else if (tradeSetKey.equals(SKY_TRADER_RARE)) rolls = UniformGenerator.between(1, 2);
            else if (tradeSetKey.equals(SKY_TRADER_SNACKS)) rolls = UniformGenerator.between(3, 4);

            context.register(tradeSetKey, new TradeSet(HolderSet.direct(holders), rolls, false, Optional.empty()));
        });
    }
}
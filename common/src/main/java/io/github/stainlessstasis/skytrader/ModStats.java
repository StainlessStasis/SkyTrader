package io.github.stainlessstasis.skytrader;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats {
    public static final Identifier FLIGHTS_TAKEN = ModConstants.id("flights_taken");

    public static void init() {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, FLIGHTS_TAKEN, FLIGHTS_TAKEN);
        Stats.CUSTOM.get(FLIGHTS_TAKEN, StatFormatter.DEFAULT);
    }
}
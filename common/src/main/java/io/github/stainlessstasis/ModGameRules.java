package io.github.stainlessstasis;

import io.github.stainlessstasis.skytrader.platform.Services;
import net.minecraft.world.level.gamerules.*;

import java.util.function.Supplier;

public class ModGameRules {
    public static final Supplier<GameRule<Boolean>> SPAWN_SKY_TRADERS = registerBoolean("spawn_sky_traders", GameRuleCategory.SPAWNING, true);
    public static final Supplier<GameRule<Boolean>> SKY_TRADER_GHAST_REVENGE = registerBoolean("sky_trader_ghast_revenge", GameRuleCategory.MOBS, true);

    private static Supplier<GameRule<Boolean>> registerBoolean(String id, GameRuleCategory category, boolean defaultValue) {
        return Services.REGISTRY.registerBooleanGameRule(id, category, defaultValue);
    }

    public static void init() {}
}

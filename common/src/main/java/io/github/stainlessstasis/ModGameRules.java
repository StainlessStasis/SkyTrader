package io.github.stainlessstasis;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import io.github.stainlessstasis.skytrader.ModConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;

import java.util.function.ToIntFunction;

public class ModGameRules {
    public static final GameRule<Boolean> SPAWN_SKY_TRADERS = registerBoolean("spawn_sky_traders", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SKY_TRADER_GHAST_REVENGE = registerBoolean("sky_trader_ghast_revenge", GameRuleCategory.MOBS, true);

    private static GameRule<Boolean> registerBoolean(String id, GameRuleCategory category, boolean defaultValue) {
        return register(
                id, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL,
                defaultValue, FeatureFlagSet.of(),
                GameRuleTypeVisitor::visitBoolean, b -> b ? 1 : 0
        );
    }

    private static <T> GameRule<T> register(
            String id,
            GameRuleCategory category,
            GameRuleType typeHint,
            ArgumentType<T> argumentType,
            Codec<T> codec,
            T defaultValue,
            FeatureFlagSet requiredFeatures,
            GameRules.VisitorCaller<T> visitorCaller,
            ToIntFunction<T> commandResultFunction
    ) {
        return Registry.register(
                BuiltInRegistries.GAME_RULE,
                ModConstants.id(id),
                new GameRule<>(category, typeHint, argumentType, visitorCaller, codec, commandResultFunction, defaultValue, requiredFeatures)
        );
    }

    public static void init() {}
}

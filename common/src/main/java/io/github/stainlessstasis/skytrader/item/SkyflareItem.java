package io.github.stainlessstasis.skytrader.item;

import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class SkyflareItem extends FireworkRocketItem {
    public SkyflareItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        var result = super.use(level, player, hand);
        if (result == InteractionResult.SUCCESS && level instanceof ServerLevel serverLevel) {
            summonSkyTrader(player, serverLevel);
        }
        return result;
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        if (result == InteractionResult.SUCCESS && context.getLevel() instanceof ServerLevel serverLevel) {
            summonSkyTrader(context.getPlayer(), serverLevel);
        }
        return result;
    }

    public void summonSkyTrader(Player player, ServerLevel level) {
        SkyTraderSpawner.forceSpawn(player, level);
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.YOU_CALLED);
            if (level.dimension() != Level.OVERWORLD) {
                ModAdvancements.grant(serverPlayer, ModAdvancements.THERES_NO_SKY_HERE);
            }
        }
    }
}

package io.github.stainlessstasis.skytrader.item;

import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.minecraft.server.level.ServerLevel;
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
        if (level instanceof ServerLevel serverLevel) {
            summonSkyTrader(player, serverLevel);
        }
        return super.use(level, player, hand);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            summonSkyTrader(context.getPlayer(), serverLevel);
        }
        return super.useOn(context);
    }

    public void summonSkyTrader(Player player, ServerLevel level) {
        SkyTraderSpawner.forceSpawn(player, level);
    }
}

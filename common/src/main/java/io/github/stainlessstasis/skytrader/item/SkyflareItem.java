package io.github.stainlessstasis.skytrader.item;

import io.github.stainlessstasis.skytrader.ModAdvancements;
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
        if (result == InteractionResult.SUCCESS) {
            if (level instanceof ServerLevel serverLevel) {
                summonSkyTrader(player, serverLevel);
            }
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);
        }
        return result;
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        var result = super.useOn(context);
        if (result == InteractionResult.SUCCESS) {
            if (context.getLevel() instanceof ServerLevel serverLevel) {
                summonSkyTrader(context.getPlayer(), serverLevel);
            }
            if (context.getPlayer() instanceof Player player) {
                player.getCooldowns().addCooldown(context.getItemInHand(), 100);
            }
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
            if (serverPlayer.isFallFlying()) {
                ModAdvancements.grant(serverPlayer, ModAdvancements.A_BIT_REDUNDANT);
            }
        }
    }
}

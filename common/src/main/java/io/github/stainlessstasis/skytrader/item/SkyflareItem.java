package io.github.stainlessstasis.skytrader.item;

import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class SkyflareItem extends FireworkRocketItem {
    public SkyflareItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        if (player.isFallFlying()) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            if (player.dropAllLeashConnections(null)) {
                level.playSound(null, player, SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            Projectile.spawnProjectile(new FireworkRocketEntity(level, itemStack, player), serverLevel, itemStack);
            itemStack.consume(1, player);
            summonSkyTrader(player, serverLevel);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null && player.isFallFlying()) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            ItemStack itemStack = context.getItemInHand();
            Vec3 clickLocation = context.getClickLocation();
            Direction direction = context.getClickedFace();
            Projectile.spawnProjectile(
                    new FireworkRocketEntity(level, context.getPlayer(),
                    clickLocation.x + (double)direction.getStepX() * 0.15,
                    clickLocation.y + (double)direction.getStepY() * 0.15,
                    clickLocation.z + (double)direction.getStepZ() * 0.15,
                    itemStack),

                    serverLevel, itemStack
            );
            itemStack.shrink(1);
            summonSkyTrader(player, serverLevel);
        }

        return InteractionResult.SUCCESS;
    }

    public void summonSkyTrader(Player player, ServerLevel level) {
        SkyTraderSpawner.forceSpawn(player, level);
    }
}

package io.github.stainlessstasis.skytrader.item;

import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class SkyfareBeaconItem extends Item {
    public SkyfareBeaconItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        itemStack.consume(1, player);
        player.getCooldowns().addCooldown(itemStack, 100);

        if (level instanceof ServerLevel serverLevel) {
            SkyTraderSpawner.forceSpawn(player, serverLevel);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}

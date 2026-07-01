package io.github.stainlessstasis.skytrader.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WanderingTrader.class)
public interface WanderingTraderInvoker {
    @Invoker("getWanderTarget")
    @Nullable BlockPos invokeGetWanderTarget();
}

package io.github.stainlessstasis.skytrader.mixin;

import io.github.stainlessstasis.skytrader.SkyTraderSavedData;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KilledTrigger.class)
public class KilledTriggerMixin {
    /**
     * Prevents Uneasy Alliance from being obtained by killing a former Sky Trader Ghast that turned hostile
     */
    @Inject(method = "trigger", at = @At("HEAD"), cancellable = true)
    private void revokeForFormerTraderGhasts(ServerPlayer player, Entity entity, DamageSource killingBlow, CallbackInfo ci) {
        if (!(entity instanceof Ghast)) return;
        ServerLevel serverLevel = player.level();

        SkyTraderSavedData savedData = SkyTraderSavedData.get(serverLevel);
        if (!savedData.formerTraderGhasts.contains(entity.getUUID())) return;

        savedData.formerTraderGhasts.remove(entity.getUUID());
        savedData.setDirty();

        ci.cancel();
    }
}

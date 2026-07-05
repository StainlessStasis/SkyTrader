package io.github.stainlessstasis.skytrader.mixin;

import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public abstract class HappyGhastMixin {
    @Inject(method = "scanPlayerAboveGhast", at = @At("HEAD"), cancellable = true)
    private void skipScanDuringEndOfRide(CallbackInfoReturnable<Boolean> cir) {
        if ((HappyGhast)(Object) this instanceof SkyTraderGhast ghast && ghast.getRideState().isEndOfRide()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}

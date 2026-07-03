package io.github.stainlessstasis.skytrader.mixin;

import io.github.stainlessstasis.skytrader.entity.SkyTrader;
import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    /**
     * Vanilla skips entities that share the viewer's root vehicle.
     * SkyTrader/SkyTraderGhast need to be clickable even while the player rides the same ghast.
     */
    @Redirect(
            method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;",
                    ordinal = 0
            )
    )
    private static Entity skytrader$bypassSameVehicleExclusion(Entity entity) {
        if (entity instanceof SkyTrader || entity instanceof SkyTraderGhast) {
            return null;
        }
        return entity.getRootVehicle();
    }
}
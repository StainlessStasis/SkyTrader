package io.github.stainlessstasis.skytrader.mixin;

import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HappyGhast.class)
public interface HappyGhastInvoker {
    @Invoker("setServerStillTimeout")
    void invokeSetServerStillTimeout(int timeout);
}

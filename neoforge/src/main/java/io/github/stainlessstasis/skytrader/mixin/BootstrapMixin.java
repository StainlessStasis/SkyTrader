package io.github.stainlessstasis.skytrader.mixin;

import io.github.stainlessstasis.ModGameRules;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;bootStrap()V"))
    private static void registerGameRulesBeforeFreeze(CallbackInfo ci) {
        ModGameRules.init();
    }
}
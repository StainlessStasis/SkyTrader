package com.example.examplemod.mixin;

import com.example.examplemod.trader.SkyTraderSpawner;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the Sky Trader's spawner into the overworld's custom spawners
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow
    @Final
    private SavedDataStorage savedDataStorage;

    @ModifyExpressionValue(
            method = "createLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"
            )
    )
    private ImmutableList<CustomSpawner> injectSkyTraderSpawner(ImmutableList<CustomSpawner> original) {
        List<CustomSpawner> spawners = new ArrayList<>(original);
        spawners.add(new SkyTraderSpawner(this.savedDataStorage));
        return ImmutableList.copyOf(spawners);
    }
}

package com.example.examplemod;

import com.example.examplemod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;

public class SkyTraderModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntities.SKY_TRADER, WanderingTraderRenderer::new);
        EntityRenderers.register(ModEntities.SKY_TRADER_GHAST, HappyGhastRenderer::new);
    }
}

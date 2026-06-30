package com.example.examplemod;

import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber
public class SkyTraderModClient {
    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SkyTraderMod.TRADER_HOLDER.get(), WanderingTraderRenderer::new);
        event.registerEntityRenderer(SkyTraderMod.GHAST_HOLDER.get(), HappyGhastRenderer::new);
    }
}

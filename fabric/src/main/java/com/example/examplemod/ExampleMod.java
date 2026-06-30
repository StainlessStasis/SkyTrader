package com.example.examplemod;

import com.example.examplemod.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.npc.villager.Villager;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ModEntities.SKY_TRADER = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ModEntities.TRADER_KEY,
                ModEntities.TRADER_BUILDER.build(ModEntities.TRADER_KEY)
        );
        FabricDefaultAttributeRegistry.register(ModEntities.SKY_TRADER, Villager.createAttributes());

        ModEntities.SKY_TRADER_GHAST = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ModEntities.GHAST_KEY,
                ModEntities.GHAST_BUILDER.build(ModEntities.GHAST_KEY)
        );
        FabricDefaultAttributeRegistry.register(ModEntities.SKY_TRADER_GHAST, HappyGhast.createAttributes());
    }
}

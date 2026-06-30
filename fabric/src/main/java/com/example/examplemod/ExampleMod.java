package com.example.examplemod;

import com.example.examplemod.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.npc.villager.Villager;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ResourceKey<EntityType<?>> skyTraderKey = ResourceKey.create(Registries.ENTITY_TYPE, ModConstants.id("sky_trader"));
        ModEntities.SKY_TRADER = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                skyTraderKey,
                ModEntities.TRADER_BUILDER.build(skyTraderKey)
        );
        FabricDefaultAttributeRegistry.register(ModEntities.SKY_TRADER, Villager.createAttributes());

        ResourceKey<EntityType<?>> skyTraderGhastKey = ResourceKey.create(Registries.ENTITY_TYPE, ModConstants.id("sky_trader_ghast"));
        ModEntities.SKY_TRADER_GHAST = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                skyTraderGhastKey,
                ModEntities.GHAST_BUILDER.build(skyTraderGhastKey)
        );
        FabricDefaultAttributeRegistry.register(ModEntities.SKY_TRADER_GHAST, HappyGhast.createAttributes());
    }
}

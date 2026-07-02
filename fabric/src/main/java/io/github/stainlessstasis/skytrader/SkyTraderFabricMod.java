package io.github.stainlessstasis.skytrader;

import io.github.stainlessstasis.ModGameRules;
import io.github.stainlessstasis.skytrader.entity.ModEntities;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.CreativeModeTabs;

public class SkyTraderFabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // entities
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

        // misc
        ModItems.init();
        ModGameRules.init();
        FabricCommands.registerCommands();

        // creative tab
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(creativeTab -> {
                    creativeTab.accept(ModItems.SKYFARE_TICKET.get());
                });
    }
}

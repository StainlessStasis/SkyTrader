package com.example.examplemod;

import com.example.examplemod.entity.ModEntities;
import com.example.examplemod.entity.SkyTrader;
import com.example.examplemod.entity.SkyTraderGhast;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ModConstants.MOD_ID)
public class SkyTraderMod {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ModConstants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SkyTrader>> TRADER_HOLDER =
            ENTITIES.register(
                    ModEntities.TRADER_KEY.identifier().getPath(),
                    () -> ModEntities.TRADER_BUILDER.build(ModEntities.TRADER_KEY)
            );

    public static final DeferredHolder<EntityType<?>, EntityType<SkyTraderGhast>> GHAST_HOLDER =
            ENTITIES.register(
                    ModEntities.GHAST_KEY.identifier().getPath(),
                    () -> ModEntities.GHAST_BUILDER.build(ModEntities.GHAST_KEY)
            );

    public SkyTraderMod(IEventBus eventBus) {
        ENTITIES.register(eventBus);

        eventBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) {
                ModEntities.SKY_TRADER = TRADER_HOLDER.get();
                ModEntities.SKY_TRADER_GHAST = GHAST_HOLDER.get();
            }
        });

        eventBus.addListener(this::registerAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SKY_TRADER, Villager.createAttributes().build());
        event.put(ModEntities.SKY_TRADER_GHAST, HappyGhast.createAttributes().build());
    }
}
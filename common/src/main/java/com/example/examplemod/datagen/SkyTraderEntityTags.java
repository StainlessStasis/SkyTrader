package com.example.examplemod.datagen;

import com.example.examplemod.entity.ModEntities;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.function.BiConsumer;

public final class SkyTraderEntityTags {
    public static void generate(BiConsumer<TagKey<EntityType<?>>, EntityType<?>> gen) {
        gen.accept(EntityTypeTags.CAN_EQUIP_HARNESS, ModEntities.SKY_TRADER_GHAST);
    }
}
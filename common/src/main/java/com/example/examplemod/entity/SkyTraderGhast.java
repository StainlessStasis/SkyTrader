package com.example.examplemod.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class SkyTraderGhast extends HappyGhast implements TraceableEntity, OwnableEntity {
    private @Nullable EntityReference<LivingEntity> owner;

    public SkyTraderGhast(EntityType<? extends HappyGhast> type, Level level) {
        super(type, level);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return OwnableEntity.super.getOwner();
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return owner;
    }
}
